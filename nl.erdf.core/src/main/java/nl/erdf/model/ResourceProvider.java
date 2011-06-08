/**
 * 
 */
package nl.erdf.model;

import java.util.Set;

import nl.erdf.datalayer.DataLayer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

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
	abstract public Node getResource(Node_Variable variable, Solution solution, DataLayer dataLayer);

	/**
	 * @return the set of variables used by the provider
	 */
	abstract public Set<Node_Variable> getVariables();
}
