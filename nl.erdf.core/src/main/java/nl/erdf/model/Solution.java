package nl.erdf.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import nl.erdf.util.Format;

import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A solution to a request is a set of bindings
 * 
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Solution implements Comparable<Solution> {
	/** Logger */
	protected final Logger logger = LoggerFactory.getLogger(Solution.class);

	// The age of that solution
	private int age = 0;

	// The fitness of the solution
	private double fitness = 0;

	// Is that an optimal solution?
	private boolean isOptimal = false;

	// Index the variables by name (sorted tree is important for the hash code)
	private final Map<String, Variable> variables = new TreeMap<String, Variable>();

	/**
	 * @param variable
	 */
	public void add(Variable variable) {
		variables.put(variable.getName(), variable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Solution clone() {
		Solution solution = new Solution();

		// Perform a deep copy of the variables
		for (Variable variable : this.variables.values())
			solution.add(variable.clone());

		return solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Solution other) {
		if (other.getTotalReward() < this.getTotalReward())
			return 1;

		if (this.equals(other)) {
			return 0;
		}

		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Solution other = (Solution) obj;

		// Compare the assignments
		for (String var : variables.keySet()) {
			if (this.getValue(var) == null && other.getValue(var) != null)
				return false;
			else if (this.getValue(var) != null && other.getValue(var) == null)
				return false;
			else if (this.getValue(var) != null && !this.getValue(var).equals(other.getValue(var)))
				return false;
		}

		return true;
	}

	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @return the total reward of the variables
	 */
	public double getTotalReward() {
		double sum = 0;
		for (Variable variable : this.variables.values())
			sum += variable.getReward();
		return sum;
	}

	/**
	 * Shortcut for getVariable(variableName).getValue()
	 * 
	 * @param variableName
	 * @return the value bound to the variable
	 */
	public Value getValue(String variableName) {
		if (!variables.containsKey(variableName))
			return null;
		return variables.get(variableName).getValue();
	}

	/**
	 * @param variableName
	 * @return the variable
	 */
	public Variable getVariable(String variableName) {
		if (!variables.containsKey(variableName))
			return null;
		return variables.get(variableName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (Variable var : variables.values())
			result = prime * result + ((var.getValue() == null) ? 0 : var.getValue().hashCode());
		return result;
	}

	/**
	 * @return the isOptimal
	 */
	public boolean isOptimal() {
		return isOptimal;
	}

	/**
	 * @param age
	 *            the age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}

	/**
	 * @param isOptimal
	 *            the isOptimal to set
	 */
	public void setOptimal(boolean isOptimal) {
		this.isOptimal = isOptimal;
	}

	/**
	 * @return the number of variables
	 */
	public int size() {
		return variables.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(Format.format.format(getFitness())).append(" ").append(" [");
		for (Variable variable : this.variables.values()) {
			buffer.append(variable.getName()).append("=").append(variable.getValue()).append(" (");
			buffer.append(Format.format.format(variable.getReward())).append("),");
		}
		buffer.setCharAt(buffer.length() - 1, ']');
		return buffer.append(" age=").append(getAge()).append(" " + this.hashCode()).toString();
	}

	/**
	 * 
	 */
	public void resetScores() {
		setOptimal(false);
		fitness = 0;
		for (Variable variable : this.variables.values())
			variable.setReward(0.0);
	}

	/**
	 * @param fitness
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * @return the fitness value
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * @return the list of variables
	 */
	public Collection<Variable> getVariables() {
		return variables.values();
	}
}
