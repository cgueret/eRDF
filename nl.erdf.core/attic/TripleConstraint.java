package nl.erdf.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.QueryPattern;
import nl.erdf.model.Constraint;
import nl.erdf.model.Request;
import nl.erdf.model.ResourceProvider;
import nl.erdf.model.Solution;

/**
 * @author cgueret
 * 
 */
public class TripleConstraint implements Constraint, ResourceProvider {
	// The graph pattern is a triple with variables in it
	protected final Triple graphPattern;

	// The set of blacklisted triples
	protected Set<Triple> blackListedTriples = null;

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
	public void setBlackListedTriples(Set<Triple> blackListedTriples) {
		this.blackListedTriples = blackListedTriples;
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
		solution.getBinding((Node_Variable) node).incrementReward(reward);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getVariables()
	 */
	@Override
	public Set<Node_Variable> getVariables() {
		Set<Node_Variable> vars = new HashSet<Node_Variable>();
		if (graphPattern.getSubject().isVariable())
			vars.add((Node_Variable) graphPattern.getSubject());
		if (graphPattern.getPredicate().isVariable())
			vars.add((Node_Variable) graphPattern.getPredicate());
		if (graphPattern.getObject().isVariable())
			vars.add((Node_Variable) graphPattern.getObject());
		return vars;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.model.ResourceProvider#getQuery(com.hp.hpl.jena.graph.Node_Variable
	 * , nl.erdf.model.Solution)
	 */
	@Override
	public QueryPattern getQuery(Node_Variable variable, Solution solution) {
		// Instantiate the pattern into a full triple
		Node subject = graphPattern.getSubject();
		if (graphPattern.getSubject().isVariable())
			subject = solution.getBinding((Node_Variable) graphPattern.getSubject()).getValue();
		Node predicate = graphPattern.getPredicate();
		if (graphPattern.getPredicate().isVariable())
			predicate = solution.getBinding((Node_Variable) graphPattern.getPredicate()).getValue();
		Node object = graphPattern.getObject();
		if (graphPattern.getObject().isVariable())
			object = solution.getBinding((Node_Variable) graphPattern.getObject()).getValue();

		// Replace the target variable by a return and create a query
		QueryPattern query = null;
		if ((graphPattern.getSubject().isVariable()) && (variable.equals(graphPattern.getSubject())))
			query = new QueryPattern(QueryPattern.RETURN, predicate, object);
		if ((graphPattern.getPredicate().isVariable()) && (variable.equals(graphPattern.getPredicate())))
			query = new QueryPattern(subject, QueryPattern.RETURN, object);
		if ((graphPattern.getObject().isVariable()) && (variable.equals(graphPattern.getObject())))
			query = new QueryPattern(subject, predicate, QueryPattern.RETURN);

		return query;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.model.ResourceProvider#getExpectedReward(nl.erdf.model.Request,
	 * com.hp.hpl.jena.graph.Node_Variable, nl.erdf.model.Solution)
	 */
	@Override
	public double getExpectedReward(Request request, Node_Variable variable, Solution solution) {
		double reward = 0;

		if (graphPattern.getSubject().isVariable())
			reward += solution.getBinding((Node_Variable) graphPattern.getSubject()).getReward()
					/ request.getMaximumReward((Node_Variable) graphPattern.getSubject());
		else
			reward += 1;

		if (graphPattern.getPredicate().isVariable())
			reward += solution.getBinding((Node_Variable) graphPattern.getPredicate()).getReward()
					/ request.getMaximumReward((Node_Variable) graphPattern.getPredicate());
		else
			reward += 1;

		if (graphPattern.getObject().isVariable())
			reward += solution.getBinding((Node_Variable) graphPattern.getObject()).getReward()
					/ request.getMaximumReward((Node_Variable) graphPattern.getObject());
		else
			reward += 1;

		return reward / 3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getRewards(nl.erdf.model.Solution,
	 * nl.erdf.datalayer.DataLayer)
	 */
	@Override
	public Map<Node_Variable, Double> getRewards(Solution solution, DataLayer dataLayer) {
		Map<Node_Variable, Double> rewards = new HashMap<Node_Variable, Double>(5);

		// Instantiate the triple based on the given solution
		Node subject = graphPattern.getSubject();
		if (subject.isVariable())
			subject = solution.getBinding((Node_Variable) subject).getValue();
		Node predicate = graphPattern.getPredicate();
		if (predicate.isVariable())
			predicate = solution.getBinding((Node_Variable) predicate).getValue();
		Node object = graphPattern.getObject();
		if (object.isVariable())
			object = solution.getBinding((Node_Variable) object).getValue();
		Triple triple = Triple.create(subject, predicate, object);

		// Check if it is a black listed triple and eventually return right away
		if (blackListedTriples != null && blackListedTriples.contains(triple)) {
			if (graphPattern.getSubject().isVariable())
				rewards.put((Node_Variable) graphPattern.getSubject(), LOW_REWARD);
			if (graphPattern.getPredicate().isVariable())
				rewards.put((Node_Variable) graphPattern.getPredicate(), LOW_REWARD);
			if (graphPattern.getObject().isVariable())
				rewards.put((Node_Variable) graphPattern.getObject(), LOW_REWARD);
		} else {
			// Check if the triple is valid and eventually return right away
			if (dataLayer.isValid(triple)) {
				if (graphPattern.getSubject().isVariable())
					rewards.put((Node_Variable) graphPattern.getSubject(), HIGH_REWARD);
				if (graphPattern.getPredicate().isVariable())
					rewards.put((Node_Variable) graphPattern.getPredicate(), HIGH_REWARD);
				if (graphPattern.getObject().isVariable())
					rewards.put((Node_Variable) graphPattern.getObject(), HIGH_REWARD);
			} else

			// In the following, we test partial bindings. One part of the
			// triple
			// is removed at a time and the validity of the rest is tested

			// Handle the case where S is a variable
			if (graphPattern.getSubject().isVariable() && dataLayer.isValid(Triple.create(Node.ANY, predicate, object))) {
				if (graphPattern.getPredicate().isVariable())
					rewards.put((Node_Variable) graphPattern.getPredicate(), MEDIUM_REWARD);
				if (graphPattern.getObject().isVariable())
					rewards.put((Node_Variable) graphPattern.getObject(), MEDIUM_REWARD);
			} else

			// Handle the case where P is a variable
			if (graphPattern.getPredicate().isVariable() && dataLayer.isValid(Triple.create(subject, Node.ANY, object))) {
				if (graphPattern.getSubject().isVariable())
					rewards.put((Node_Variable) graphPattern.getSubject(), MEDIUM_REWARD);
				if (graphPattern.getObject().isVariable())
					rewards.put((Node_Variable) graphPattern.getObject(), MEDIUM_REWARD);
			} else

			// Handle the case where O is a variable
			if (graphPattern.getObject().isVariable() && dataLayer.isValid(Triple.create(subject, predicate, Node.ANY))) {
				if (graphPattern.getSubject().isVariable())
					rewards.put((Node_Variable) graphPattern.getSubject(), MEDIUM_REWARD);
				if (graphPattern.getPredicate().isVariable())
					rewards.put((Node_Variable) graphPattern.getPredicate(), MEDIUM_REWARD);
			}
		}
	}

}
