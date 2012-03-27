/**
 * 
 */
package nl.erdf.constraints;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Reward {
	/** Low reward given to false bindings */
	public final static double NULL = 0;

	/** Low reward given for blacklisted triples */
	public final static double LOW = 0.25;

	/** Low reward given to bindings partially validating a triple */
	public final static double MEDIUM = 0.5;

	/** Low reward given to valid bindings */
	public final static double HIGH = 1;

}
