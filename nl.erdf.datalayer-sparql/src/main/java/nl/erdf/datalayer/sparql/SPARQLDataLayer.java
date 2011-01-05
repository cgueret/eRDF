package nl.erdf.datalayer.sparql;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Random;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.QueryPattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author tolgam
 * 
 */
public class SPARQLDataLayer extends Observable implements DataLayer {
	// Logger instance
	protected final Logger logger = LoggerFactory.getLogger(SPARQLDataLayer.class);

	// The directory
	private final Directory directory;

	// Query cache for gets
	private final Cache cache;

	// If BLOCKING is set to true, no MAYBE answer will be allowed
	// every function will block until the final results are known
	private static final boolean BLOCKING = false;

	/**
	 * @param directory
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public SPARQLDataLayer(Directory directory) throws FileNotFoundException, IOException {
		this.directory = directory;
		cache = new Cache(directory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see datalayer.DataLayer#isValid(model.Node, model.Node, model.Node)
	 */
	@Override
	public boolean isValid(final Triple triple) {
		// Create a query pattern
		QueryPattern query = new QueryPattern(triple);

		// Deal with obvious answers... if there is BLANK in the pattern it is
		// sure the answer is no
		if (query.contains(null) || query.contains(Node.NULL))
			return false;

		// Now, the normal cases
		Node s = triple.getSubject();
		Node p = triple.getPredicate();
		Node o = triple.getObject();
		if (query.contains(Node.ANY))
			return isPartiallyValid(s, p, o);
		else
			return isFullyValid(s, p, o);
	}

	/**
	 * @param s
	 * @param p
	 * @param o
	 * @return
	 */
	private boolean isPartiallyValid(final Node s, final Node p, final Node o) {
		// Create the relevant partial query pattern
		Node s2 = (s.equals(Node.ANY) ? QueryPattern.RETURN : s);
		Node p2 = (p.equals(Node.ANY) ? QueryPattern.RETURN : p);
		Node o2 = (o.equals(Node.ANY) ? QueryPattern.RETURN : o);
		QueryPattern partialQueryPattern = new QueryPattern(s2, p2, o2);

		// If the set is not empty, we are sure the triple is partially valid
		NodeSet resources = cache.get(partialQueryPattern);
		if (!resources.isEmpty())
			return true;

		// If the content of the set is final, we are sure the pattern is not
		// valid
		if (resources.isFinal())
			return false;

		// If blocking, wait until the result has some result in it
		if (BLOCKING) {
			resources.waitForSomeContent();
			return (!resources.isEmpty());
		}

		// Not sure yet, something may be added in the future
		return false;
	}

	/**
	 * Test if a fully instantiated triple is valid. First, check if the object
	 * is cached amongst one of the *PO index. Then, is needed, check if the
	 * object is cached in a SP* index. If that also fails, ask for the SP* and
	 * wait to see what result comes out. NOTE: asking for *PO may not help as it
	 * may request subjects having generic PO such as rdf:type Person. The SP*
	 * are expected to lead to fewer results and a complete check.
	 * 
	 * @param s
	 *           The subject
	 * @param p
	 *           The predicate
	 * @param o
	 *           The object
	 * @return True if this triple exists, false otherwise
	 */
	private boolean isFullyValid(final Node s, final Node p, final Node o) {

		// Try to see if we have seen SP* before. We start with that
		// one as we assume there will be less SP->O than PO->S
		// (for instance, <X,Type,Thing> versus <blah,Type,Y>)
		QueryPattern partialSP = new QueryPattern(s, p, QueryPattern.RETURN);
		NodeSet resourcesSP = cache.get(partialSP);
		// logger.info("(1) " + partialSP.toQueryString() + " " +
		// resourcesSP.contains(o) + "/" + resourcesSP.isFinal() + " " + o + " " +
		// o.getClass());
		if (resourcesSP.contains(o))
			return true;

		// If SP is final, we are sure about the answer
		if (resourcesSP.isFinal()) {
			// logger.info("Content: " + resourcesSP.toString());
			return false;
		}

		// Try to see if we have seen *PO before
		QueryPattern partialPO = new QueryPattern(QueryPattern.RETURN, p, o);
		NodeSet resourcesPO = cache.get(partialPO);
		if (resourcesPO.contains(s))
			return true;

		// If the content of PO is final, then the definitive answer is a NO
		if (resourcesPO.isFinal())
			return false;

		// If blocking, wait until resultSP has some result in it
		// we bet on it as it is likely to be the smallest
		if (BLOCKING) {
			resourcesSP.waitForFinalContent();
			return resourcesSP.contains(o);
		}

		// Maybe future content will arrive for one of the two sets,
		// meanwhile we are not sure of the result
		// logger.info("" + new QueryPattern(s, p, o));
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see datalayer.DataLayer#getRandomResource(ec.util.MersenneTwisterFast,
	 * datalayer.wod.QueryPattern)
	 */
	@Override
	public Node getRandomResource(Random rand, final QueryPattern queryPattern) {
		// Default resourceSet to use
		NodeSet resources = NodeSet.EMPTY_SET;

		// Get a set of resources from the cache
		resources = cache.get(queryPattern);

		// If blocking, wait until the result set has something in it
		if (BLOCKING)
			resources.waitForSomeContent();

		// Return a random resource from the set
		return resources.get(rand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see datalayer.DataLayer#getNumberOfResources(datalayer.wod.QueryPattern)
	 */
	@Override
	public long getNumberOfResources(final QueryPattern queryPattern) {
		// Try to get the result from the cache
		NodeSet resources = cache.get(queryPattern);

		// If blocking, wait until the result set is finished
		if (BLOCKING)
			resources.waitForFinalContent();

		// If there is nothing in it but we are still updating, then the size
		// is unknown
		if ((!resources.isFinal()) && (resources.size() == 0))
			return -1;

		return resources.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.main.datalayer.DataLayer#clear()
	 */
	@Override
	public void clear() {
		cache.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		shutdown();
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.main.datalayer.DataLayer#shutdown()
	 */
	@Override
	public void shutdown() {
		for (EndPoint endpoint : directory.endPoints())
			endpoint.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.main.datalayer.DataLayer#waitForData()
	 */
	@Override
	public void waitForLatencyBuffer() {
		// This function blocks until there is on average less that 10
		// jobs per end point queuing to be executed
		double load = Double.MAX_VALUE;
		while (load > 10) {
			load = 0;
			for (EndPoint endpoint : directory.endPoints())
				load += endpoint.getQueueSize();
			load = load / directory.endPoints().size();

			// We don't have
			try {
				Thread.sleep((long) (50 + 2 * load));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
