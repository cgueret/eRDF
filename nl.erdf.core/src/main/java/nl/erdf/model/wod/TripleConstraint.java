package nl.erdf.model.wod;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Constraint;
import nl.erdf.model.Solution;
import nl.erdf.model.Variable;

/**
 * @author cgueret
 *
 */
public class TripleConstraint implements Constraint {
	// The graph pattern is a triple with variables in it
	protected final Triple graphPattern;

	// The set of blacklisted triples
	protected TripleSet blackListedTriples = null;

	/** Low reward given to false bindings */
	public final static double NULL_REWARD = 0;

	/** Low reward given for blacklisted triples */
	public final static double LOW_REWARD = 0.25;

	/** Low reward given to bindings partially validating a triple */
	public final static double MEDIUM_REWARD = 0.5;

	/** Low reward given to valid bindings */
	public final static double HIGH_REWARD = 1;

	/**
	 * @param s
	 * @param p
	 * @param o
	 */
	public TripleConstraint(Node s, Node p, Node o) {
		this.graphPattern = Triple.create(s, p, o);
	}

	/**
	 * @param triple
	 */
	public TripleConstraint(Triple triple) {
		this.graphPattern = triple;
	}

	/**
	 * @param blackListedTriples
	 */
	public void setBlackListedTriples(TripleSet blackListedTriples) {
		this.blackListedTriples = blackListedTriples;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.main.model.Constraint#assignRewards(nl.erdf.main.model.Solution,
	 * nl.erdf.main.datalayer.DataLayer)
	 */
	@Override
	public void assignRewards(Solution solution, DataLayer dataLayer) {
		// Instantiate the triple based on the given solution
		Node subject = graphPattern.getSubject();
		if (subject.isVariable())
			subject = solution.getBinding((Variable) subject).getValue();
		Node predicate = graphPattern.getPredicate();
		if (predicate.isVariable())
			predicate = solution.getBinding((Variable) predicate).getValue();
		Node object = graphPattern.getObject();
		if (object.isVariable())
			object = solution.getBinding((Variable) object).getValue();
		Triple triple = Triple.create(subject, predicate, object);

		// Check if it is a black listed triple and eventually return right away
		if (blackListedTriples.contains(triple)) {
			assignReward(solution, graphPattern.getSubject(), LOW_REWARD);
			assignReward(solution, graphPattern.getPredicate(), LOW_REWARD);
			assignReward(solution, graphPattern.getObject(), LOW_REWARD);
			return;
		}

		// Check if the triple is valid and eventually return right away
		if (dataLayer.isValid(triple)) {
			assignReward(solution, graphPattern.getSubject(), HIGH_REWARD);
			assignReward(solution, graphPattern.getPredicate(), HIGH_REWARD);
			assignReward(solution, graphPattern.getObject(), HIGH_REWARD);
			return;
		}

		// In the following, we test partial bindings. One part of the triple
		// is removed at a time and the validity of the rest is tested

		// Handle the case where S is a variable
		if (graphPattern.getSubject().isVariable()) {
			if (dataLayer.isValid(Triple.create(Node.ANY, predicate, object))) {
				assignReward(solution, graphPattern.getPredicate(), MEDIUM_REWARD);
				assignReward(solution, graphPattern.getObject(), MEDIUM_REWARD);
				return;
			}
		}

		// Handle the case where P is a variable
		if (graphPattern.getPredicate().isVariable()) {
			if (dataLayer.isValid(Triple.create(subject, Node.ANY, object))) {
				assignReward(solution, graphPattern.getSubject(), MEDIUM_REWARD);
				assignReward(solution, graphPattern.getObject(), MEDIUM_REWARD);
				return;
			}
		}

		// Handle the case where O is a variable
		if (graphPattern.getObject().isVariable()) {
			if (dataLayer.isValid(Triple.create(subject, predicate, Node.ANY))) {
				assignReward(solution, graphPattern.getSubject(), MEDIUM_REWARD);
				assignReward(solution, graphPattern.getPredicate(), MEDIUM_REWARD);
				return;
			}
		}
	}

	/**
	 * Assign a reward to a particular binding
	 * 
	 * @param object
	 * @param reward
	 */
	private void assignReward(Solution solution, Node node, double reward) {
		// We can only assign rewards to variable bindings
		if (!node.isVariable())
			return;

		// Credit the binding with the reward
		solution.getBinding((Variable) node).incrementReward(reward);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.main.model.Constraint#getPart(int)
	 */
	@Override
	public Node getPart(int position) {
		if (position == 0)
			return graphPattern.getSubject();
		if (position == 1)
			return graphPattern.getPredicate();
		if (position == 2)
			return graphPattern.getObject();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return graphPattern.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.main.model.Constraint#getSize()
	 */
	@Override
	public int getSize() {
		return 3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return graphPattern.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		return graphPattern.equals(other);
	}

}
