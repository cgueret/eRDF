package nl.erdf.datalayer;

import java.util.Random;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * A data layer is an abstraction over a given number of data sources it is used
 * to get resources from this sources
 * 
 * @author tolgam
 */
public interface DataLayer {
	/**
	 * Indicates the number of resources available for a given queryPattern
	 * 
	 * @param queryPattern
	 * @return the number of resources available for that query
	 */
	public abstract long getNumberOfResources(final QueryPattern queryPattern);

	/**
	 * Only the list of the nodes is cached. The node actually returned is
	 * randomly selected from this list
	 * 
	 * @param random
	 * @param queryPattern
	 * @return a random term (URI/Literal/BNode)
	 */
	public abstract Node getRandomResource(Random random, QueryPattern queryPattern);

	/**
	 * Check is the combination of S,P and O is valid according to one of the
	 * available end points. This combination may contains blank nodes and/or
	 * wildcards
	 * 
	 * @param s
	 *           subject node
	 * @param p
	 *           predicate node
	 * @param o
	 *           object node
	 * @return true if combination is valid
	 */
	public abstract boolean isValid(Triple triple);

	/**
	 * Clear the content of the data layer, relevant in particular if a cache is
	 * involved
	 */
	public abstract void clear();

	/**
	 * Called when the data layer is not used anymore
	 */
	public abstract void shutdown();

	/**
	 * This method blocks until more data is available from the data layer
	 */
	public abstract void waitForLatencyBuffer();

}
