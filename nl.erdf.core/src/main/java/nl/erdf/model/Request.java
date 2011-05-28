package nl.erdf.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.erdf.datalayer.DataLayer;

/**
 * @author Christophe Guéret <cgueret@few.vu.nl>
 * 
 */
public abstract class Request {
	static final Logger logger = LoggerFactory.getLogger(Request.class);
	
	/** List of variables that compose the request */
	protected final List<Variable> variables = new ArrayList<Variable>();

	/** List of constraints that compose the request */
	protected final List<Constraint> constraints = new ArrayList<Constraint>();

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
	public Iterable<Variable> variables() {
		return variables;
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
		for (Binding var : solution.bindings()) {
			fitness += var.getReward();
			maximumReward += var.getMaximumReward();
		}
		fitness = fitness / maximumReward;

		return fitness;
	}

	/**
	 * Add a new constraint to the request. At the same time, link all the
	 * variable concerned by the constraint to that constraint.
	 * 
	 * @param constraint
	 * @return the newly added constraint
	 */
	public Constraint add(Constraint constraint) {
		// Add the constraint
		logger.info("Add " + constraint);
		constraints.add(constraint);

		// Tell the variables they are now part of that constraint
		for (int i = 0; i < constraint.getSize(); i++) {
			Object part = constraint.getPart(i);
			System.out.println(part.getClass());
			if (part instanceof Variable)
				((Variable) part).addConstraint(constraint);
		}

		return constraint;
	}

	/**
	 * Duplicate safe registration of a variable
	 * 
	 * @param variable
	 *           a variable to register
	 * @return the variable
	 */
	public Variable add(Variable variable) {
		// If that variable is already known, return it
		if (variables.contains(variable))
			return variables.get(variables.indexOf(variable));

		// Add the variable
		variables.add(variable);

		return variable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String buffer = "Variables : " + variables.size() + "\n";
		buffer += "Constraints :\n";
		for (Constraint cstr : constraints)
			buffer += cstr.toString() + "\n";
		return buffer;
	}

}
