package nl.erdf.model;

import org.openrdf.model.Value;
import org.openrdf.query.algebra.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// http://www.openrdf.org/doc/sesame2/api/

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 *
 */
public class Binding {
	final static Logger logger = LoggerFactory.getLogger(Binding.class);
	
	/** The variable part of the binding */
	private Var variable;

	/** The node the variable is bound to */
	private Value value;

	/** The reward assigned for this binding */
	private double reward;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + reward + "] " + variable + "=" + value;
	}

	/**
	 * @param variable
	 * 
	 */
	public Binding(Var variable) {
		this(variable, null);
	}

	/**
	 * @param variable
	 * @param value
	 */
	public Binding(Var variable, Value value) {
		this.variable = variable;
		this.value = value;
		reward = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		Binding newBinding = new Binding(variable, value);
		return newBinding;
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
		if (!(obj instanceof Binding))
			return false;
		Binding other = (Binding) obj;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/**
	 * @return the current value associated to that variable
	 */
	public Value getValue() {
		return value;
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
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * @return the variable
	 */
	public Var getVariable() {
		return variable;
	}

	/**
	 * It is not allowed to change a grounded variable
	 * 
	 * @param value
	 */
	public void setValue(Value value) {
		this.value = value;
	}

	/**
	 * 
	 */
	public void resetReward() {
		reward = 0;
	}

	/**
	 * @return the amount of reward received
	 */
	public double getReward() {
		return reward;
	}

	/**
	 * @param value
	 */
	public void incrementReward(double value) {
		reward += value;
	}

	/**
	 * @param dd
	 */
	public void rescaleReward(double dd) {
		reward = reward * dd;
	}
}
