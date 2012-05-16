/**
 * 
 */
package nl.erdf.model;

import java.util.Set;

import nl.erdf.datalayer.DataLayer;

import org.openrdf.model.Value;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public interface ResourceProvider {
	/**
	 * @param variableName
	 * @param solution
	 * @param dataLayer
	 * @return a resource
	 */
	abstract public Value getResource(String variableName, Solution solution, DataLayer dataLayer);

	/**
	 * @return the set of variables used by the provider
	 */
	abstract public Set<String> getVariables();

	/**
	 * @param variableName
	 * @param solution
	 * @param dataLayer
	 * @return the number of resources the provider can serve
	 */
	abstract public long getNumberResources(String variableName, Solution solution, DataLayer dataLayer);

}
