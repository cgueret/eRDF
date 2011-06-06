/**
 * 
 */
package nl.erdf.model;

import java.util.Set;

import nl.erdf.datalayer.QueryPattern;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public interface ResourceProvider {
	/**
	 * @param variable
	 * @param solution
	 * @return
	 */
	abstract public QueryPattern getQuery(Node_Variable variable, Solution solution);

	/**
	 * @param request
	 * @param variable
	 * @param solution
	 * @return
	 */
	abstract public double getExpectedReward(Request request, Node_Variable variable, Solution solution);

	/**
	 * @return
	 */
	abstract public Set<Node_Variable> getVariables();
}
