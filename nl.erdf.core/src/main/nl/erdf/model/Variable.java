/**
 * 
 */
package nl.erdf.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node_Variable;

/**
 * @author tolgam
 * 
 */
public class Variable extends Node_Variable {
	/** Logger class */
	static final Logger logger = LoggerFactory.getLogger(Variable.class);

	/** List of constraints concerned by this variable */
	private List<Constraint> constraints = new ArrayList<Constraint>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see main.model.Resource#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see main.model.Resource#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Variable))
			return false;

		return super.label.equals(((Variable) obj).label);
	}

	/**
	 * @param label
	 */
	public Variable(String label) {
		super(label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * @return an iterable of the constraints
	 */
	public Iterable<Constraint> constraints() {
		return constraints;
	}

	/**
	 * @param constraint
	 */
	public void addConstraint(Constraint constraint) {
		constraints.add(constraint);
		// logger.debug(this.label + " in " + constraint + " "
		// + constraints.size());
	}

	/**
	 * @return the number of constraints
	 */
	public int getConstraintsSize() {
		return constraints.size();
	}
}
