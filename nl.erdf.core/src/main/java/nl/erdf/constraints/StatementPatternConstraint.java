package nl.erdf.constraints;

import java.util.HashSet;
import java.util.Set;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.ResourceProvider;
import nl.erdf.model.Solution;
import nl.erdf.model.StatementSet;
import nl.erdf.util.RandomNumber;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

/**
 * @author cgueret
 * 
 */
public class StatementPatternConstraint implements Constraint, ResourceProvider {
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pattern.toString();
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
	@Override
	public Set<Var> getVariables() {
		Set<Var> result = new HashSet<Var>();
		result.addAll(pattern.getVarList());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.Constraint#getReward(nl.erdf.model.Solution,
	 * nl.erdf.datalayer.DataLayer)
	 */
	@Override
	public double getReward(Solution solution, DataLayer dataLayer, StatementSet blackList) {
		// Duplicate the statement and try to instantiate it
		StatementPattern tmp = pattern.clone();
		for (Var var : tmp.getVarList())
			if (var.getValue() == null)
				var.setValue(solution.getValue(var.getName()));

		// Count the number of remaining null values
		int nullValues = 0;
		for (Var var : tmp.getVarList())
			if (var.getValue() == null)
				nullValues++;

		Resource s = (Resource) tmp.getSubjectVar().getValue();
		URI p = (URI) tmp.getPredicateVar().getValue();
		Value o = tmp.getObjectVar().getValue();

		// First case where the pattern is fully instantiated
		if (nullValues == 0) {
			Statement stmt = new StatementImpl(s, p, o);

			// Check if it is a black listed triple and eventually return right
			// away
			if (blackList != null && blackList.contains(stmt))
				return Reward.LOW;

			// Check if the triple is valid and eventually return right away
			if (dataLayer.isValid(stmt))
				return Reward.HIGH;
		} else {
			// If we could change either S or O to get a solution, return a
			// MEDIUM reward
			if (pattern.getSubject().isVariable() && pattern.getObject().isVariable())
				if (dataLayer.isValid(Triple.create(subject, predicate, Node.ANY))
						|| dataLayer.isValid(Triple.create(Node.ANY, predicate, object)))
					return Reward.MEDIUM;

			// Same if we could change P
			if (pattern.getPredicate().isVariable())
				if (dataLayer.isValid(Triple.create(subject, Node.ANY, object)))
					return Reward.MEDIUM;
		}

		// TODO Return double[]{REWARD,REWARD,REWARD} for fine-grained rewarding
		return Reward.NULL;
	}

	/**
	 * @param solution
	 * @return the instanciated triple
	 */
	public Statement getInstanciatedTriple(Solution solution) {
		Value subject = pattern.getSubject();
		if (subject.isVariable())
			subject = solution.getBinding((Var) subject).getValue();
		Value predicate = pattern.getPredicate();
		if (predicate.isVariable())
			predicate = solution.getBinding((Var) predicate).getValue();
		Value object = pattern.getObject();
		if (object.isVariable())
			object = solution.getBinding((Var) object).getValue();

		return Triple.create(subject, predicate, object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.model.ResourceProvider#getResource(com.hp.hpl.jena.graph.
	 * Node_Variable, nl.erdf.model.Solution, nl.erdf.datalayer.DataLayer)
	 */
	@Override
	public Node getResource(Var variable, Solution solution, DataLayer dataLayer) {
		// Instantiate the pattern but keep the sought variable
		Node subject = pattern.getSubject();
		if (pattern.getSubject().isVariable() && !pattern.getSubject().equals(variable))
			subject = solution.getBinding((Var) pattern.getSubject()).getValue();
		Node predicate = pattern.getPredicate();
		if (pattern.getPredicate().isVariable() && !pattern.getPredicate().equals(variable))
			predicate = solution.getBinding((Var) pattern.getPredicate()).getValue();
		Node object = pattern.getObject();
		if (pattern.getObject().isVariable() && !pattern.getObject().equals(variable))
			object = solution.getBinding((Var) pattern.getObject()).getValue();

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

