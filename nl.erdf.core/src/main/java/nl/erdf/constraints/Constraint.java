package nl.erdf.constraints;

import java.util.Set;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Solution;
import nl.erdf.model.StatementSet;

import org.openrdf.query.algebra.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 * @param blackList
	 * @return the reward for that constraint
	 */
	abstract double getReward(Solution solution, DataLayer dataLayer, StatementSet blackList);

	/**
	 * @return the set of variables used in the constraint
	 */
	abstract Set<Var> getVariables();
}
