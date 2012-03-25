/**
 * 
 */
package nl.erdf.model;

import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.algebra.Var;

import nl.erdf.datalayer.DataLayer;

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
	abstract public Value getResource(Var variable, Solution solution, DataLayer dataLayer);

	/**
	 * @return the set of variables used by the provider
	 */
	abstract public Set<Var> getVariables();
}
