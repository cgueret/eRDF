/**
 * 
 */
package nl.erdf.model;

import org.openrdf.model.Value;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Variable {
	// The name of the variable
	private final String name;

	// The value assigned to it
	private Value value = null;

	// The reward the binding was credited with
	private double reward = 0.0;

	/**
	 * @param name
	 */
	public Variable(String name) {
		this.name = name;
	}

	/**
	 * @param name
	 * @param value
	 */
	public Variable(String name, Value value) {
		this(name);
		setValue(value);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the reward
	 */
	public double getReward() {
		return reward;
	}

	/**
	 * @param reward
	 *            the reward to set
	 */
	public void setReward(double reward) {
		this.reward = reward;
	}

	/**
	 * @return the value
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Value value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Variable clone() {
		Variable v = new Variable(name);
		v.value = value;
		v.reward = reward;
		return v;
	}
}
