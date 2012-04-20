package nl.erdf.constraints;

import java.util.Set;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Solution;
import nl.erdf.model.TripleSet;

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
	abstract RewardsTable getRewards(Solution solution, DataLayer dataLayer, TripleSet blackList);

	/**
	 * @return the set of variables used in the constraint
	 */
	abstract Set<String> getVariables();
}
