package nl.erdf.datalayer;

import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;


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
	 * @param pattern
	 * @precondition There is only one and only one WILDCARD value
	 * @return the number of resources available for that query
	 */
	abstract long getNumberOfResources(StatementPattern pattern);

	/**
	 * @param pattern
	 * @return a set of resources matching the triple
	 */
	abstract Value getResource(StatementPattern pattern);

	/**
	 * Check is the combination of S,P and O is valid according to one of the
	 * available end points. This combination may contains blank nodes and/or
	 * wildcards
	 * 
	 * @param triple
	 *            the Triple to check
	 * @return true if the triple is valid
	 */
	public abstract boolean isValid(StatementPattern pattern);

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
