package nl.erdf.model;

import nl.erdf.datalayer.DataLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

/**
 * @author tolgam
 * 
 */
public interface Constraint {
	/** Common logger */
	final Logger logger = LoggerFactory.getLogger(Constraint.class);

	/**
	 * @param solution
	 * @param dataLayer
	 */
	abstract void assignRewards(Solution solution, DataLayer dataLayer);

	/**
	 * @param position
	 * @return a part of the constraint
	 */
	abstract Node getPart(int position);

	/**
	 * Returns the size of a constraint, that is its number of parts
	 * 
	 * @return the size of the constraint
	 */
	abstract int getSize();

}
