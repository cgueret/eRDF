/**
 * 
 */
package nl.erdf.constraints;

import java.util.HashSet;
import java.util.Set;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Solution;

import org.openrdf.model.Statement;
import org.openrdf.query.algebra.Var;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class StatementPatternSetConstraint implements Constraint {
	/** The constraints in this block */
	private final Set<StatementPatternConstraint> constraints = new HashSet<StatementPatternConstraint>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getReward(nl.erdf.model.Solution,
	 * nl.erdf.datalayer.DataLayer)
	 */
	@Override
	public double getReward(Solution solution, DataLayer dataLayer) {
		double max = 0;
		for (StatementPatternConstraint cstr : constraints) {
			double reward = cstr.getReward(solution, dataLayer);
			if (reward > max)
				max = reward;
		}
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getVariables()
	 */
	@Override
	public Set<Var> getVariables() {
		Set<Var> vars = new HashSet<Var>();
		for (StatementPatternConstraint cstr : constraints)
			vars.addAll(cstr.getVariables());
		return vars;
	}

	/**
	 * @param tripleConstraint
	 */
	public void add(StatementPatternConstraint tripleConstraint) {
		constraints.add(tripleConstraint);
	}

	/**
	 * @param solution
	 * @return the instanciated triples
	 */
	public Set<Statement> getInstanciatedTriples(Solution solution) {
		Set<Statement> triples = new HashSet<Statement>();
		for (StatementPatternConstraint cstr : constraints)
			triples.add(cstr.getInstanciatedTriple(solution));
		return triples;
	}

}
