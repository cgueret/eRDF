package nl.erdf.constraints;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Constraint;
import nl.erdf.model.ResourceProvider;
import nl.erdf.model.Solution;
import nl.erdf.util.RandomNumber;

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
		if (!(other instanceof TripleConstraint))
			return false;
		return graphPattern.equals(((TripleConstraint) other).graphPattern);
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
	 * @see nl.erdf.model.Constraint#getReward(nl.erdf.model.Solution,
	 * nl.erdf.datalayer.DataLayer)
	 */
	@Override
	public double getReward(Solution solution, DataLayer dataLayer) {
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

		// Check if it is a black listed triple and eventually return right away
		if (blackListedTriples != null && blackListedTriples.contains(Triple.create(subject, predicate, object)))
			return LOW_REWARD;

		// Check if the triple is valid and eventually return right away
		if (dataLayer.isValid(Triple.create(subject, predicate, object)))
			return HIGH_REWARD;

		// If we could change either S or O to get a solution, return a MEDIUM
		// reward
		if (graphPattern.getSubject().isVariable() && graphPattern.getObject().isVariable())
			if (dataLayer.isValid(Triple.create(subject, predicate, Node.ANY))
					|| dataLayer.isValid(Triple.create(Node.ANY, predicate, object)))
				return MEDIUM_REWARD;

		// Same if we could change P
		if (graphPattern.getPredicate().isVariable())
			if (dataLayer.isValid(Triple.create(subject, Node.ANY, object)))
				return MEDIUM_REWARD;

		// FIXME misses some combinations like ??O or S??
		// TODO Return double[]{REWARD,REWARD,REWARD} for fine-grained rewarding
		return NULL_REWARD;
	}

	/**
	 * @param solution
	 * @return the instanciated triple
	 */
	public Triple getInstanciatedTriple(Solution solution) {
		Node subject = graphPattern.getSubject();
		if (subject.isVariable())
			subject = solution.getBinding((Node_Variable) subject).getValue();
		Node predicate = graphPattern.getPredicate();
		if (predicate.isVariable())
			predicate = solution.getBinding((Node_Variable) predicate).getValue();
		Node object = graphPattern.getObject();
		if (object.isVariable())
			object = solution.getBinding((Node_Variable) object).getValue();

		return Triple.create(subject, predicate, object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.ResourceProvider#getResource(com.hp.hpl.jena.graph.
	 * Node_Variable, nl.erdf.model.Solution, nl.erdf.datalayer.DataLayer)
	 */
	@Override
	public Node getResource(Node_Variable variable, Solution solution, DataLayer dataLayer) {
		// Instantiate the pattern but keep the sought variable
		Node subject = graphPattern.getSubject();
		if (graphPattern.getSubject().isVariable() && !graphPattern.getSubject().equals(variable))
			subject = solution.getBinding((Node_Variable) graphPattern.getSubject()).getValue();
		Node predicate = graphPattern.getPredicate();
		if (graphPattern.getPredicate().isVariable() && !graphPattern.getPredicate().equals(variable))
			predicate = solution.getBinding((Node_Variable) graphPattern.getPredicate()).getValue();
		Node object = graphPattern.getObject();
		if (graphPattern.getObject().isVariable() && !graphPattern.getObject().equals(variable))
			object = solution.getBinding((Node_Variable) graphPattern.getObject()).getValue();

		// Pick one of the possible node at random
		Node node = Node.NULL;
		Set<Node> nodes = dataLayer.getResources(Triple.create(subject, predicate, object));
		if (nodes.size() > 0) {
			int index = RandomNumber.nextInt(nodes.size());
			node = (Node) nodes.toArray()[index];
		} else {
			// logger.info("bad! " + Triple.create(subject, predicate, object));
			if (blackListedTriples != null)
				blackListedTriples.add(Triple.create(subject, predicate, object));
		}

		return node;
	}
}

// Replace the target variable by a return and create a query
/*
 * QueryPattern query = null; if ((graphPattern.getSubject().isVariable()) &&
 * (variable.equals(graphPattern.getSubject()))) query = new
 * QueryPattern(QueryPattern.RETURN, predicate, object); if
 * ((graphPattern.getPredicate().isVariable()) &&
 * (variable.equals(graphPattern.getPredicate()))) query = new
 * QueryPattern(subject, QueryPattern.RETURN, object); if
 * ((graphPattern.getObject().isVariable()) &&
 * (variable.equals(graphPattern.getObject()))) query = new
 * QueryPattern(subject, predicate, QueryPattern.RETURN);
 */

