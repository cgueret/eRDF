package nl.erdf.datalayer;

import nl.erdf.model.Triple;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;

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
	abstract long getNumberOfResources(Triple pattern);

	/**
	 * @param pattern
	 * @return a set of resources matching the triple
	 */
	abstract Value getResource(Triple pattern);

	/**
	 * Check is the combination of S,P and O is valid. This combination may
	 * contain at most one null value
	 * 
	 * @param pattern
	 *            the pattern to check
	 * @return true if the triple is valid
	 */
	public boolean isValid(Triple pattern);

	/**
	 * @param statement
	 */
	public void add(Statement statement);

	/**
	 * Clear the content of the data layer, relevant in particular if a cache is
	 * involved
	 */
	public void clear();

	/**
	 * Called when the data layer is not used anymore
	 */
	public void shutdown();

	/**
	 * This method blocks until more data is available from the data layer
	 */
	public void waitForLatencyBuffer();

}
