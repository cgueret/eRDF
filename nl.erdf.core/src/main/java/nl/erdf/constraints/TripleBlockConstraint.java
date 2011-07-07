/**
 * 
 */
package nl.erdf.constraints;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Constraint;
import nl.erdf.model.Solution;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class TripleBlockConstraint implements Constraint {
	/** The constraints in this block */
	private final Set<TripleConstraint> tripleConstraints = new HashSet<TripleConstraint>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getReward(nl.erdf.model.Solution,
	 * nl.erdf.datalayer.DataLayer)
	 */
	public double getReward(Solution solution, DataLayer dataLayer) {
		double max = 0;
		for (TripleConstraint cstr : tripleConstraints) {
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
	public Set<Node_Variable> getVariables() {
		Set<Node_Variable> vars = new HashSet<Node_Variable>();
		for (TripleConstraint cstr : tripleConstraints)
			vars.addAll(cstr.getVariables());
		return vars;
	}

	/**
	 * @param tripleConstraint
	 */
	public void add(TripleConstraint tripleConstraint) {
		tripleConstraints.add(tripleConstraint);
	}
	
	/**
	 * @param solution
	 * @return the instanciated triples
	 */
	public Set<Triple> getInstanciatedTriples(Solution solution) {
		Set<Triple> triples = new HashSet<Triple>();
		for (TripleConstraint cstr : tripleConstraints)
			triples.add(cstr.getInstanciatedTriple(solution));
		return triples;
	}

}
