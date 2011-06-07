package nl.erdf.model;

import java.util.Set;

import nl.erdf.datalayer.DataLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author tolgam
 * 
 */
public interface Constraint {
	/** Common logger */
	final Logger logger = LoggerFactory.getLogger(Constraint.class);

	/**
	 * @param solution
	 * @param dataLayer
	 */
	abstract double getReward(Solution solution, DataLayer dataLayer);

	/**
	 * @return the set of variables used in the constraint
	 */
	abstract Set<Node_Variable> getVariables();
}
