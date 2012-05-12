package nl.erdf.constraints.impl;

import java.util.HashSet;
import java.util.Set;

import nl.erdf.constraints.Constraint;
import nl.erdf.constraints.Reward;
import nl.erdf.constraints.RewardsTable;
import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Solution;
import nl.erdf.model.Triple;
import nl.erdf.model.TripleSet;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StatementPatternConstraint [pattern=" + pattern + "]";
	}

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

		// We can not reward triples with more than one null
		if (t.getNumberNulls() > 1)
			return rewards;

		// Check if it is a black listed triple
		if (t.getNumberNulls() == 0 && blackList != null && blackList.contains(t)) {
			// Assign the minimal reward to all the variables
			for (String variable : getVariables())
				rewards.set(variable, Reward.LOW);
			return rewards;
		}

		// Check if the triple is valid
		if (dataLayer.isValid(t)) {
			// It was a fully instanciated triple
			if (t.getNumberNulls() == 0) {
				// Assign the maximum reward to all the variables
				for (String variable : getVariables())
					rewards.set(variable, Reward.HIGH);
				return rewards;
			}

			// There was a null in it
			if (t.getNumberNulls() == 1) {
				if (!pattern.getSubjectVar().hasValue())
					if (solution.getVariable(pattern.getSubjectVar().getName()).getValue() != null)
						rewards.set(pattern.getSubjectVar().getName(), Reward.MEDIUM);
				if (!pattern.getPredicateVar().hasValue())
					if (solution.getVariable(pattern.getPredicateVar().getName()).getValue() != null)
						rewards.set(pattern.getPredicateVar().getName(), Reward.MEDIUM);
				if (!pattern.getObjectVar().hasValue())
					if (solution.getVariable(pattern.getObjectVar().getName()).getValue() != null)
						rewards.set(pattern.getObjectVar().getName(), Reward.MEDIUM);
				return rewards;
			}
		}

		// If we arrive here, that triple is wrong or has a null in it.
		if (t.getNumberNulls() == 0 && dataLayer.isValid(new Triple(t.getSubject(), t.getPredicate(), null))) {
			if (!pattern.getSubjectVar().hasValue())
				if (solution.getVariable(pattern.getSubjectVar().getName()).getValue() != null)
					rewards.set(pattern.getSubjectVar().getName(), Reward.MEDIUM);
			if (!pattern.getPredicateVar().hasValue())
				if (solution.getVariable(pattern.getPredicateVar().getName()).getValue() != null)
					rewards.set(pattern.getPredicateVar().getName(), Reward.MEDIUM);
			return rewards;
		}
		if (t.getNumberNulls() == 0 && dataLayer.isValid(new Triple(null, t.getPredicate(), t.getObject()))) {
			if (!pattern.getPredicateVar().hasValue())
				if (solution.getVariable(pattern.getPredicateVar().getName()).getValue() != null)
					rewards.set(pattern.getPredicateVar().getName(), Reward.MEDIUM);
			if (!pattern.getObjectVar().hasValue())
				if (solution.getVariable(pattern.getObjectVar().getName()).getValue() != null)
					rewards.set(pattern.getObjectVar().getName(), Reward.MEDIUM);
			return rewards;
		}
		if (t.getNumberNulls() == 0 && dataLayer.isValid(new Triple(t.getSubject(), null, t.getObject()))) {
			if (!pattern.getSubjectVar().hasValue())
				if (solution.getVariable(pattern.getSubjectVar().getName()).getValue() != null)
					rewards.set(pattern.getSubjectVar().getName(), Reward.MEDIUM);
			if (!pattern.getObjectVar().hasValue())
				if (solution.getVariable(pattern.getObjectVar().getName()).getValue() != null)
					rewards.set(pattern.getObjectVar().getName(), Reward.MEDIUM);
			return rewards;
		}

		return rewards;
	}
}
