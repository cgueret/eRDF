/**
 * 
 */
package nl.erdf.util;

import nl.erdf.model.Solution;
import nl.erdf.model.Triple;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Convert {
	/**
	 * @param var
	 * @param sol
	 * @return the value
	 */
	public static Value getValue(Var var, Solution sol) {
		Value v = null;
		if (var.hasValue())
			v = var.getValue();
		else
			v = sol.getValue(var.getName());
		return v;
	}

	/**
	 * @param statementPattern
	 * @param solution
	 * @return a triple
	 */
	public static Triple toTriple(StatementPattern statementPattern, Solution solution) {
		Value s = Convert.getValue(statementPattern.getSubjectVar(), solution);
		Value p = Convert.getValue(statementPattern.getPredicateVar(), solution);
		Value o = Convert.getValue(statementPattern.getObjectVar(), solution);

		// Non valid triple
		if (s != null && !(s instanceof Resource))
			return null;

		// Non valid triple
		if (p != null && !(p instanceof URI))
			return null;

		return new Triple((Resource) s, (URI) p, o);
	}
}
