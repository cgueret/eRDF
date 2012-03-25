package nl.erdf.model;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Value;
import org.openrdf.query.algebra.Var;


/**
 * A solution to a request is a set of bindings
 * TODO: Make this a set of StatementPattern. Move scoring outside of class
 * 
 * @author tolgam
 * 
 */
public class Solution implements Comparable<Solution> {
	// Format
	static final DecimalFormat format = new DecimalFormat("0.00");
	// Bindings
	private final Map<Var, Binding> bindings = new HashMap<Var, Binding>();
	// The age of that solution
	private int age = 0;
	// Relevance with respect to the query
	private double fitness = 0;
	// Is that an optimal solution?
	private boolean isOptimal = false;

	/**
	 * @param binding
	 */
	public void add(Binding binding) {
		bindings.put(binding.getVariable(), binding);
	}

	/**
	 * @return The collection of bindings defined by this solution
	 */
	public Collection<Binding> bindings() {
		return bindings.values();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		Solution solution = new Solution();

		// Copy the bindings
		for (Binding binding : bindings.values())
			solution.add((Binding) binding.clone());

		// Reset everything else
		solution.fitness = 0;
		solution.isOptimal = false;
		solution.age = 0;

		return solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Solution o) {
		if (o.getFitness() < this.getFitness())
			return 1;

		if (this.equals(o)) {
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
		// Easy cases
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Solution))
			return false;

		// Compare each binding
		Solution other = (Solution) obj;
		for (Binding binding : bindings.values())
			if (!other.getBinding(binding.getVariable()).getValue().equals(binding.getValue()))
				return false;

		return true;
	}

	/**
	 * @return age
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @param variable
	 * @return the binding for that variable
	 */
	public Binding getBinding(Var variable) {
		return bindings.get(variable);
	}

	/**
	 * @return the fitness of this solution
	 */
	public double getFitness() {
		return fitness;
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
		for (Binding binding : bindings.values())
			result = prime * result + ((binding == null) ? 0 : binding.hashCode());
		return result;
	}

	/**
	 * 
	 */
	public void incrementAge() {
		age++;
	}

	/**
	 * @return the isOptimal
	 */
	public boolean isOptimal() {
		return isOptimal;
	}

	/**
	 * 
	 */
	public void resetAge() {
		age = 0;
	}

	/**
	 * @param fitness
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * @param isOptimal
	 *            the isOptimal to set
	 */
	public void setOptimal(boolean isOptimal) {
		this.isOptimal = isOptimal;
	}

	/**
	 * @return the number of bindings
	 */
	public int size() {
		return bindings.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(format.format(fitness)).append(" ").append(" [");
		for (Binding b : bindings.values())
			buffer.append(b.getVariable().getName()).append("=").append(b.getValue()).append(",");
		buffer.setCharAt(buffer.length() - 1, ']');
		return buffer.append(" age=").append(age).toString();
	}

	/**
	 * Shortcut for getBinding.getValue
	 * 
	 * @param variable
	 * @return the value bound to the variable
	 */
	public Value getValue(Var variable) {
		return bindings.get(variable).getValue();
	}
}
