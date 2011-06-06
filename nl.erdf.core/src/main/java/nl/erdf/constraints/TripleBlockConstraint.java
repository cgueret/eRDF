/**
 * 
 */
package nl.erdf.constraints;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

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
	 * @see nl.erdf.model.Constraint#assignRewards(nl.erdf.model.Solution,
	 * nl.erdf.datalayer.DataLayer)
	 */
	@Override
	public void assignRewards(Solution solution, DataLayer dataLayer) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getPart(int)
	 */
	@Override
	public Node getPart(int position) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getVariables()
	 */
	@Override
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
}
