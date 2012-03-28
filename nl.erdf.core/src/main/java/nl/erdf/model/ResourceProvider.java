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
	 * @param variable
	 * @param solution
	 * @param dataLayer
	 * @return a resource
	 */
	abstract public Value getResource(String variable, Solution solution, DataLayer dataLayer);

	/**
	 * @return the set of variables used by the provider
	 */
	abstract public Set<String> getVariables();
}
