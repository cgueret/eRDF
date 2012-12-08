package nl.erdf.datalayer.sparql;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.Set;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Directory;
import nl.erdf.model.EndPoint;
import nl.erdf.model.Triple;
import nl.erdf.util.Converter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.repository.sparql.query.SPARQLBooleanQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tolgam
 * 
 */
public class SPARQLDataLayer extends Observable implements DataLayer {
	// Logger instance
	protected final Logger logger = LoggerFactory.getLogger(SPARQLDataLayer.class);

	// If BLOCKING is set to true every function will block until the final
	// results are known
	private static final boolean BLOCKING = true;

	public static final Var RETURN = new Var("erdf");

	// Query cache for gets
	private final Cache cache;

	// The client connection manager which avoids DoS
	private final ThreadSafeClientConnManager connManager;

	// Executors for processing SPARQL queries
	List<EndPointExecutor> executors = new ArrayList<EndPointExecutor>();

	/**
	 * @param directory
	 */
	public SPARQLDataLayer(Directory directory) {
		// Create a scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		// Create a connection manager
		connManager = new ThreadSafeClientConnManager(schemeRegistry);
		connManager.setDefaultMaxPerRoute(2);
		connManager.setMaxTotal(200);

		// Start the end points
		for (EndPoint endPoint : directory) {
			EndPointExecutor executor = new EndPointExecutor(endPoint);
			executor.start(connManager);
			executors.add(executor);
		}

		// Create a cache
		cache = new Cache(executors);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.main.datalayer.DataLayer#clear()
	 */
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.datalayer.DataLayer#getNumberOfResources(nl.erdf.model.impl.Triple
	 * )
	 */
	public long getNumberOfResources(Triple pattern) {
		if (pattern.getNumberNulls() != 1)
			return 0;

		// Create a pattern
		Var s = (pattern.getSubject() == null ? RETURN : new Var("s", pattern.getSubject()));
		Var p = (pattern.getPredicate() == null ? RETURN : new Var("p", pattern.getPredicate()));
		Var o = (pattern.getObject() == null ? RETURN : new Var("o", pattern.getObject()));
		StatementPattern triplePattern = new StatementPattern(s, p, o);

		// Try to get the result from the cache
		NodeSet resources = cache.get(triplePattern);

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
	 * @see nl.erdf.datalayer.DataLayer#getResource(nl.erdf.model.impl.Triple)
	 */
	public Value getResource(Triple pa) {
		if (pa.getNumberNulls() > 1)
			return null;

		// Scan the pattern to replace the variable with the return var
		Var s = (pa.getSubject() == null ? RETURN : new Var("s", pa.getSubject()));
		Var p = (pa.getPredicate() == null ? RETURN : new Var("p", pa.getPredicate()));
		Var o = (pa.getObject() == null ? RETURN : new Var("o", pa.getObject()));
		StatementPattern triplePattern = new StatementPattern(s, p, o);

		// Default resourceSet to use
		NodeSet resources = NodeSet.EMPTY_SET;

		// Get a set of resources from the cache
		resources = cache.get(triplePattern);

		// If blocking, wait until the result set has something in it
		if (BLOCKING)
			resources.waitForSomeContent();

		// logger.info(queryPattern.toString());
		// logger.info(""+resources.size());
		Set<Value> values = resources.content();
		if (values.size() > 0) {
			int index = (new Random()).nextInt(values.size());
			return (Value) values.toArray()[index];
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#isValid(nl.erdf.model.impl.Triple)
	 */
	public boolean isValid(Triple pattern) {
		// Create a pattern
		Var s = (pattern.getSubject() == null ? RETURN : new Var("s", pattern.getSubject()));
		Var p = (pattern.getPredicate() == null ? RETURN : new Var("p", pattern.getPredicate()));
		Var o = (pattern.getObject() == null ? RETURN : new Var("o", pattern.getObject()));
		StatementPattern triplePattern = new StatementPattern(s, p, o);

		// Deal with fully instantiated patterns
		if (pattern.getNumberNulls() == 0)
			return isFullyValid(triplePattern);

		// Deal with patterns with 1 variable
		if (pattern.getNumberNulls() == 1)
			return isPartiallyValid(triplePattern);

		// Deal with patterns with 2 variables
		if (pattern.getNumberNulls() == 2)
			return isValidWithTwoVars(triplePattern);
		
		// Three variables are always true
		return true;
	}

	/**
	 * @param triplePattern
	 * @return
	 * TODO fix that hack
	 */
	private boolean isValidWithTwoVars(StatementPattern triplePattern) {
		String query = "ASK {" + Converter.toN3Ask(triplePattern) + "}";
		logger.info(query);
		
		for (EndPointExecutor executor: executors) {
			EndPoint endPoint = executor.getEndPoint();
			URI uri = endPoint.getURI();
			HttpClient client = new HttpClient();
			try {
				SPARQLBooleanQuery q = new SPARQLBooleanQuery(client, uri.toString(), "", query);
				if (q.evaluate())
					return true;
			} catch (QueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Test if a fully instantiated triple is valid. First, check if the object
	 * is cached amongst one of the *PO index. Then, is needed, check if the
	 * object is cached in a SP* index. If that also fails, ask for the SP* and
	 * wait to see what result comes out. NOTE: asking for *PO may not help as
	 * it may request subjects having generic PO such as rdf:type Person. The
	 * SP* are expected to lead to fewer results and a complete check.
	 * 
	 * @param pattern
	 *            Pattern
	 * @return True if this triple exists, false otherwise
	 */
	private boolean isFullyValid(StatementPattern pattern) {
		// logger.info("[F-VALID] " + s + " " + p + " " + o);

		// Try to see if we have seen SP* before. We start with that
		// one as we assume there will be less SP->O than PO->S
		// (for instance, <X,Type,Thing> versus <blah,Type,Y>)
		StatementPattern partialSP = pattern.clone();
		partialSP.setObjectVar(RETURN);
		NodeSet resourcesSP = cache.get(partialSP);
		if (resourcesSP.contains(pattern.getObjectVar().getValue()))
			return true;
		// If SP is final, we are sure about the answer
		if (resourcesSP.isFinal())
			return false;

		// Try to see if we have seen *PO before
		StatementPattern partialPO = pattern.clone();
		partialPO.setSubjectVar(RETURN);
		NodeSet resourcesPO = cache.get(partialPO);
		if (resourcesPO.contains(pattern.getSubjectVar().getValue()))
			return true;
		// If the content of PO is final, then the definitive answer is a NO
		if (resourcesPO.isFinal())
			return false;

		// If blocking, wait until resultSP has some result in it
		// we bet on it as it is likely to be the smallest
		if (BLOCKING) {
			resourcesSP.waitForFinalContent();
			return resourcesSP.contains(pattern.getObjectVar().getValue());
		} else {
			// Maybe future content will arrive for one of the two sets,
			// meanwhile we are not sure of the result
			return false;
		}
	}

	/**
	 * @param pattern
	 * @return
	 */
	private boolean isPartiallyValid(StatementPattern pattern) {
		// logger.info("[P-VALID] " + partialQueryPattern);

		// If the set is not empty, we are sure the triple is partially valid
		NodeSet resources = cache.get(pattern);
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
		} else {
			// Not sure yet, something may be added in the future
			return false;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.main.datalayer.DataLayer#shutdown()
	 */
	public void shutdown() {
		for (EndPointExecutor executor : executors)
			executor.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.main.datalayer.DataLayer#waitForData()
	 */
	public void waitForLatencyBuffer() {
		// This function blocks until there is on average less that 4
		// jobs per end point queuing to be executed
		double load = Double.MAX_VALUE;
		while (load > 4) {
			load = 0;
			for (EndPointExecutor executor : executors)
				load += executor.getQueueSize();
			load = load / executors.size();

			try {
				Thread.sleep((long) (50 + 2 * load));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#add(org.openrdf.model.Statement)
	 */
	public void add(Statement statement) {
		// Read only
	}
}
