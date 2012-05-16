package nl.erdf.constraints.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import nl.erdf.constraints.Constraint;
import nl.erdf.constraints.RewardsTable;
import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Solution;
import nl.erdf.model.TripleSet;

/**
 * @author cgueret
 * 
 */
public class StatementPatternSetConstraint implements Constraint {
	// The graph pattern is a triple with variables in it
	private final Set<StatementPatternConstraint> patterns = new HashSet<StatementPatternConstraint>();

	// The context of this set
	private final String context;

	/**
	 * @param context
	 */
	public StatementPatternSetConstraint(String context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StatementPatternConstraint [patterns=" + patterns + "]";
	}

	/**
	 * @param constraint
	 */
	public void add(StatementPatternConstraint constraint) {
		patterns.add(constraint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return patterns.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof StatementPatternSetConstraint))
			return false;
		return patterns.equals(((StatementPatternSetConstraint) other).patterns);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getVariables()
	 */
	public Set<String> getVariables() {
		Set<String> result = new HashSet<String>();
		for (StatementPatternConstraint pattern : patterns)
			result.addAll(pattern.getVariables());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getReward(nl.erdf.model.Solution,
	 * nl.erdf.datalayer.DataLayer)
	 */
	public RewardsTable getRewards(Solution solution, DataLayer dataLayer, TripleSet blackList) {
		RewardsTable rewards = new RewardsTable();
		double r = 0;
		for (StatementPatternConstraint pattern : patterns) {
			RewardsTable rewardsTmp = pattern.getRewards(solution, dataLayer, blackList);
			double a = 0;
			for (Entry<String, Double> reward : rewardsTmp.getRewards())
				a += reward.getValue();
			if (a > r) {
				r = a;
				rewards = rewardsTmp;
			}
		}

		return rewards;
	}

	/**
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @return the pattern constraints
	 */
	public Collection<StatementPatternConstraint> getPatternConstraints() {
		return patterns;
	}
}
