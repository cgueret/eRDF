package nl.erdf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.erdf.constraints.Constraint;
import nl.erdf.constraints.StatementPatternSetConstraint;
import nl.erdf.constraints.StatementPatternConstraint;
import nl.erdf.datalayer.DataLayer;

import org.openrdf.model.Statement;
import org.openrdf.query.algebra.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <cgueret@few.vu.nl>
 * 
 */
public abstract class Request {
	// Logger
	static final Logger logger = LoggerFactory.getLogger(Request.class);

	/** List of constraints that compose the request */
	protected final List<Constraint> constraints = new ArrayList<Constraint>();

	/** Mapping of variable -> constraints */
	protected final Map<Var, Set<Constraint>> constraintsMap = new HashMap<Var, Set<Constraint>>();

	/** Mapping of variable -> resource providers */
	protected final Map<Var, Set<ResourceProvider>> providersMap = new HashMap<Var, Set<ResourceProvider>>();

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
	public Iterable<Var> variables() {
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
		// Clear and initialise the flags and value
		double fitness = 0;
		solution.setOptimal(false);
		for (Binding binding : solution.bindings())
			binding.resetReward();

		// Check the constraints and dispatch rewards
		for (Constraint constraint : constraints) {
			double reward = constraint.getReward(solution, dataLayer);
			fitness += reward;
			for (Var variable : constraint.getVariables())
				if (!solution.getBinding(variable).getValue().equals(Node.NULL))
					solution.getBinding(variable).incrementReward(reward);
		}

		// Return the fitness value
		return fitness / constraints.size();
	}

	/**
	 * @param variable
	 * @return the maximum reward this variable can get
	 */
	public double getMaximumReward(Var variable) {
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
		// logger.info("Add constraint " + constraint);
		constraints.add(constraint);

		// Find the variables and map them they are now part of that constraint
		for (Var v : constraint.getVariables()) {
			Set<Constraint> set = constraintsMap.get(v);
			if (set == null) {
				set = new HashSet<Constraint>();
				constraintsMap.put(v, set);
			}
			set.add(constraint);
		}

		return constraint;
	}

	/**
	 * Add a new resource provider to the request. At the same time, link all
	 * the variable concerned by the provider to that provider.
	 * 
	 * @param provider
	 * @return the newly added constraint
	 */
	public ResourceProvider addResourceProvider(ResourceProvider provider) {
		// logger.info("Add provider " + provider);

		// Find the variables and map them they are now part of that constraint
		for (Var v : provider.getVariables()) {
			Set<ResourceProvider> set = providersMap.get(v);
			if (set == null) {
				set = new HashSet<ResourceProvider>();
				providersMap.put(v, set);
			}
			set.add(provider);
		}

		return provider;
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
	public Set<Statement> getTripleSet(Solution solution, boolean filter) {
		Set<Statement> triples = new HashSet<Statement>();

		for (Constraint constraint : constraints()) {
			if (constraint instanceof StatementPatternConstraint) {
				Statement triple = ((StatementPatternConstraint) constraint).getInstanciatedTriple(solution);
				if (!filter || dataLayer.isValid(triple))
					triples.add(triple);
			}
			if (constraint instanceof StatementPatternSetConstraint) {
				for (Statement triple : ((StatementPatternSetConstraint) constraint).getInstanciatedTriples(solution))
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
	public Set<Statement> getTripleSet(Solution solution) {
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

	/**
	 * @param variable
	 * @return a set of providers
	 */
	public Set<ResourceProvider> getProvidersFor(Var variable) {
		return providersMap.get(variable);
	}

}
