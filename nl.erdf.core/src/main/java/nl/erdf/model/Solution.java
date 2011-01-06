package nl.erdf.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A solution to a request is a set of bindings
 * 
 * @author tolgam
 * 
 */
public class Solution implements Comparable<Solution> {
	// Bindings
	private final ArrayList<Binding> bindings = new ArrayList<Binding>();

	// Relevance with respect to the query
	private double fitness = 0;

	// The age of that solution
	private int age = 0;

	// Is that an optimal solution?
	private boolean isOptimal = false;

	/**
	 * @param variable
	 * @return the binding for that variable
	 */
	public Binding getBinding(final Variable variable) {
		for (int i = 0; i < size(); i++)
			if (get(i).getVariable().equals(variable))
				return get(i);

		return null;
	}

	/**
	 * @param i
	 * @return
	 */
	private Binding get(int i) {
		return bindings.get(i);
	}

	/**
	 * @return the number of bindings
	 */
	public int size() {
		return bindings.size();
	}

	/**
	 * @param variable
	 * @return true if the solution has a binding for that variable
	 */
	public boolean containsVariable(final Variable variable) {
		for (int i = 0; i < size(); i++)
			if (get(i).getVariable().equals(variable))
				return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#toString()
	 */
	@Override
	public String toString() {
		String str = " (rel=" + fitness + "|opt=" + isOptimal + "|age=" + age + ")\n";

		for (Binding b : bindings) {
			str += b.getReward() + "/" + b.getMaximumReward() + " ";
			str += b.getVariable() + " = ";
			str += (b.getValue() != null ? b.getValue() : "null") + "\n";
		}

		return str;
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
		for (Binding binding : bindings)
			solution.add((Binding) binding.clone());

		// Reset everything else
		solution.fitness = 0;
		solution.isOptimal = false;
		solution.age = 0;

		return solution;
	}

	/**
	 * @param binding
	 */
	public void add(Binding binding) {
		bindings.add(binding);
	}

	/**
	 * @param fitness
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
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
		for (Binding binding : bindings)
			result = prime * result + ((binding == null) ? 0 : binding.hashCode());
		return result;
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

		if (!(obj instanceof Solution))
			return false;
		Solution other = (Solution) obj;
		for (Binding binding : bindings) {
			if (!other.getBinding(binding.getVariable()).getValue().equals(binding.getValue())) {
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Solution o) {
		if (o.getFitness() < this.getFitness())
			return 1;

		if (this.equals(o)) {
			return 0;
		}

		return -1;
	}

	/**
	 * @return The collection of bindings defined by this solution
	 */
	public Collection<Binding> bindings() {
		return bindings;
	}

	/**
	 * @param isOptimal
	 *           the isOptimal to set
	 */
	public void setOptimal(boolean isOptimal) {
		this.isOptimal = isOptimal;
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
	public void incrementAge() {
		age++;
	}

	/**
	 * @return
	 */
	public int getAge() {
		return age;
	}

	/**
	 * 
	 */
	public void resetAge() {
		age = 0;
	}
}
