/**
 * 
 */
package nl.erdf.util;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Converter {
	/**
	 * @param pattern
	 * @return
	 */
	public static String toN3(StatementPattern pattern) {
		StringBuffer t = new StringBuffer();

		if (pattern.getSubjectVar().hasValue())
			t.append(toN3(pattern.getSubjectVar().getValue()));
		else
			t.append("?").append(pattern.getSubjectVar().getName());
		t.append(" ");

		if (pattern.getPredicateVar().hasValue())
			t.append(toN3(pattern.getPredicateVar().getValue()));
		else
			t.append("?").append(pattern.getPredicateVar().getName());
		t.append(" ");

		if (pattern.getObjectVar().hasValue())
			t.append(toN3(pattern.getObjectVar().getValue()));
		else
			t.append("?").append(pattern.getObjectVar().getName());
		t.append(".");

		return t.toString();
	}

	/**
	 * @param value
	 * @return
	 */
	public static String toN3(Value value) {
		if (value instanceof Resource)
			return "<" + value.stringValue() + ">";
		if (value instanceof BNode)
			return value.stringValue();
		if (value instanceof Literal) {
			Literal v = (Literal) value;
			if (v.getDatatype() != null && v.getLanguage() == null)
				return "\"" + v.getLabel() + "\"^^<" + v.getDatatype() + ">";
			if (v.getDatatype() == null && v.getLanguage() != null)
				return "\"" + v.getLabel() + "\"@" + v.getLanguage();
			return "\"" + v.getLabel() + "\"";
		}
		return "";
	}

	/**
	 * @param value
	 */
	public static Var toVar(Value value) {
		Var variable = new Var("v" + value.hashCode());
		if (value.stringValue().startsWith("?")) {
			variable.setName(value.stringValue().substring(1));
		} else {
			variable.setAnonymous(true);
			variable.setValue(value);
		}
		return variable;
	}
}
