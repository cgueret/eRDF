package nl.erdf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.constraints.TripleConstraint;
import nl.erdf.datalayer.DataLayer;

/**
 * @author Christophe Guéret <cgueret@few.vu.nl>
 * 
 */
public abstract class Request {
	static final Logger logger = LoggerFactory.getLogger(Request.class);

	/** List of constraints that compose the request */
	protected final List<Constraint> constraints = new ArrayList<Constraint>();

	/** Mapping of variable -> constraints */
	protected final Map<Node_Variable, Set<Constraint>> constraintsMap = new HashMap<Node_Variable, Set<Constraint>>();

	/** The model on top of which this request is expressed */
	protected final DataLayer dataLayer;

	/**
	 * @param datalayer
	 * 
	 */
	public Request(final DataLayer datalayer) {
		this.dataLayer = datalayer;
	}

	/**
	 * A solution prototype is a default solution to the request. This function
	 * should be used to initialise the bindings to some default values
	 * 
	 * @return the solution prototype
	 */
	abstract public Solution getSolutionPrototype();

	/**
	 * @return the constraints
	 */
	public Iterable<Constraint> constraints() {
		return constraints;
	}

	/**
	 * @return the variables
	 */
	public Iterable<Node_Variable> variables() {
		return constraintsMap.keySet();
	}

	/**
	 * The fitness of that candidate solution is defined as the average of the
	 * individual rewards each binding received.
	 * 
	 * @param solution
	 * @return the fitness value of that candidate solution
	 */
	public double evaluate(Solution solution) {
		// Clear the flags
		solution.setOptimal(false);

		// Check the relations to assign the individual rewards to the variables
		for (Binding binding : solution.bindings())
			binding.resetReward();
		for (Constraint cstr : constraints)
			cstr.assignRewards(solution, dataLayer);

		// Compute the fitness value
		double fitness = 0;
		double maximumReward = 0;
		for (Binding binding : solution.bindings()) {
			fitness += binding.getReward();
			maximumReward += this.getMaximumReward(binding.getVariable());
		}
		fitness = fitness / maximumReward;

		return fitness;
	}

	/**
	 * @param variable
	 * @return the maximum reward this variable can get
	 */
	public double getMaximumReward(Node_Variable variable) {
		return constraintsMap.get(variable).size();
	}
	
	/**
	 * Add a new constraint to the request. At the same time, link all the
	 * variable concerned by the constraint to that constraint.
	 * 
	 * @param constraint
	 * @return the newly added constraint
	 */
	public Constraint addConstraint(Constraint constraint) {
		// Add the constraint
		logger.info("Add constraint " + constraint);
		constraints.add(constraint);

		// Find the variables and map them they are now part of that constraint
		for (int i = 0; i < constraint.getSize(); i++) {
			Node part = constraint.getPart(i);
			if (part instanceof Node_Variable) {
				Node_Variable v = (Node_Variable) part;
				Set<Constraint> set = constraintsMap.get(v);
				if (set == null) {
					set = new HashSet<Constraint>();
					constraintsMap.put(v, set);
				}
				set.add(constraint);
			}
		}

		return constraint;
	}

	/**
	 * Convert a given solution into a triple set. If filter is true, only valid
	 * triples are returned
	 * 
	 * @param solution
	 *            the solution to use to instantiate the request
	 * @param filter
	 *            if true, only valid triples are returned
	 * @return a {Set<Triple>} containing valid triples
	 */
	public Set<Triple> getTripleSet(Solution solution, boolean filter) {
		Set<Triple> triples = new HashSet<Triple>();

		for (Constraint constraint : constraints()) {
			if (constraint instanceof TripleConstraint) {
				Node s = constraint.getPart(0);
				if (s.isVariable())
					s = solution.getBinding((Node_Variable) s).getValue();

				Node p = constraint.getPart(1);
				if (p.isVariable())
					p = solution.getBinding((Node_Variable) p).getValue();

				Node o = constraint.getPart(2);
				if (o.isVariable())
					o = solution.getBinding((Node_Variable) o).getValue();

				Triple triple = Triple.create(s, p, o);
				if (!filter || dataLayer.isValid(triple))
					triples.add(triple);
			}
		}

		return triples;
	}

	/**
	 * Convert a given solution into a triple set. Equivalent to
	 * getTripleSet(solution, true)
	 * 
	 * @param solution
	 *            the solution to use to instantiate the request
	 * @return a {TripleSet} containing valid triples
	 */
	public Set<Triple> getTripleSet(Solution solution) {
		return getTripleSet(solution, true);
	}

	/*
	 * public Variable add(Variable variable) { logger.info("Add variable " +
	 * variable);
	 * 
	 * // If that variable is already known, return it if
	 * (variables.contains(variable)) return
	 * variables.get(variables.indexOf(variable));
	 * 
	 * // Add the variable variables.addConstraint(variable);
	 * 
	 * return variable; }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String buffer = "Variables : " + constraintsMap.keySet().size() + "\n";
		buffer += "Constraints :\n";
		for (Constraint cstr : constraints)
			buffer += cstr.toString() + "\n";
		return buffer;
	}

}
