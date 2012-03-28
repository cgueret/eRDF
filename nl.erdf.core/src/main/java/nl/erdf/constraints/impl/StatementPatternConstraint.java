package nl.erdf.constraints.impl;

import java.util.HashSet;
import java.util.Set;

import nl.erdf.constraints.Constraint;
import nl.erdf.constraints.Reward;
import nl.erdf.constraints.RewardsTable;
import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Solution;
import nl.erdf.model.impl.Triple;
import nl.erdf.model.impl.TripleSet;
import nl.erdf.util.Convert;

import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

/**
 * @author cgueret
 * 
 */
public class StatementPatternConstraint implements Constraint {
	// The graph pattern is a triple with variables in it
	private final StatementPattern pattern;

	/**
	 * @param pattern
	 */
	public StatementPatternConstraint(StatementPattern pattern) {
		this.pattern = pattern;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return pattern.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof StatementPatternConstraint))
			return false;
		return pattern.equals(((StatementPatternConstraint) other).pattern);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getVariables()
	 */
	public Set<String> getVariables() {
		Set<String> result = new HashSet<String>();
		for (Var var : pattern.getVarList())
			if (!var.hasValue())
				result.add(var.getName());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getReward(nl.erdf.model.Solution,
	 * nl.erdf.datalayer.DataLayer)
	 */
	public RewardsTable getRewards(Solution solution, DataLayer dataLayer, TripleSet blackList) {
		// Prepare the table
		RewardsTable rewards = new RewardsTable();

		// Instantiate the pattern with the solution
		Triple t = Convert.toTriple(pattern, solution);

		// Every pattern has at most one variable, if that variable is not
		// instantiated
		// we return no rewards (an empty table)
		if (t.getNumberNulls() != 0)
			return rewards;

		// Check if it is a black listed triple
		if (blackList != null && blackList.contains(t)) {
			// Assign the min reward to all the variables
			for (String variable : getVariables())
				rewards.set(variable, Reward.LOW);
			return rewards;
		}

		// Check if the triple is valid
		if (dataLayer.isValid(t)) {
			// Assign the max reward to all the variables
			for (String variable : getVariables())
				rewards.set(variable, Reward.HIGH);
			return rewards;
		}

		// Ok, that triple is wrong. Let's try to see if it is at least
		// partially valid
		if (dataLayer.isValid(new Triple(t.getSubject(), t.getPredicate(), null))) {
			if (!pattern.getSubjectVar().hasValue())
				rewards.set(pattern.getSubjectVar().getName(), Reward.MEDIUM);
			if (!pattern.getPredicateVar().hasValue())
				rewards.set(pattern.getPredicateVar().getName(), Reward.MEDIUM);
			return rewards;
		}
		if (dataLayer.isValid(new Triple(null, t.getPredicate(), t.getObject()))) {
			if (!pattern.getPredicateVar().hasValue())
				rewards.set(pattern.getPredicateVar().getName(), Reward.MEDIUM);
			if (!pattern.getObjectVar().hasValue())
				rewards.set(pattern.getObjectVar().getName(), Reward.MEDIUM);
			return rewards;
		}
		if (dataLayer.isValid(new Triple(t.getSubject(), null, t.getObject()))) {
			if (!pattern.getSubjectVar().hasValue())
				rewards.set(pattern.getSubjectVar().getName(), Reward.MEDIUM);
			if (!pattern.getObjectVar().hasValue())
				rewards.set(pattern.getObjectVar().getName(), Reward.MEDIUM);
			return rewards;
		}

		return rewards;
	}
}
