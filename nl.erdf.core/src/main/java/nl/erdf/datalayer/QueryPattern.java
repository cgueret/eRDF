package nl.erdf.datalayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_NULL;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.model.Variable;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class QueryPattern {
	protected final static Logger logger = LoggerFactory.getLogger(QueryPattern.class);

	/** Value used to get a result */
	public final static Node RETURN = new Variable("value");

	/** Value used to represent a wildcard */
	public final static Node WILDCARD = Node.ANY;

	// The pattern
	private final Triple pattern;

	/**
	 * @param s
	 * @param p
	 * @param o
	 */
	public QueryPattern(final Node s, final Node p, final Node o) {
		pattern = Triple.create(s, p, o);
	}

	/**
	 * @param triple
	 */
	public QueryPattern(final Triple triple) {
		pattern = triple;
	}

	/**
	 * @param o
	 * @return true if one of the nodes equals o
	 */
	public boolean contains(Object o) {
		if (!(o instanceof Node))
			return false;
		return (pattern.getSubject().equals(o) || pattern.getPredicate().equals(o) || pattern.getObject().equals(o));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return pattern.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof QueryPattern))
			return false;

		return pattern.equals(((QueryPattern) obj).pattern);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pattern.toString();
	}

	/**
	 * @return
	 * 
	 */
	// FIXME Blank nodes are not dealt with
	public String toQueryString() {
		Node node = null;
		StringBuffer buffer = new StringBuffer();

		// Subject
		node = pattern.getSubject();
		if (node instanceof Node_URI)
			buffer.append("<" + node.getURI() + ">");
		if (node instanceof Node_Variable)
			buffer.append(node.toString());
		if (node instanceof Node_NULL || node instanceof Node_ANY)
			buffer.append("?s");
		buffer.append(" ");

		// Predicate
		node = pattern.getPredicate();
		if (node instanceof Node_URI)
			buffer.append("<" + node.getURI() + ">");
		if (node instanceof Node_Variable)
			buffer.append(node.toString());
		if (node instanceof Node_NULL || node instanceof Node_ANY)
			buffer.append("?p");
		buffer.append(" ");

		// Object
		node = pattern.getObject();
		if (node instanceof Node_URI)
			buffer.append("<" + node.getURI() + ">");
		if (node instanceof Node_Variable)
			buffer.append(node.toString());
		if (node instanceof Node_Literal) {
			if (!node.getLiteralLanguage().equals(""))
				buffer.append("\"" + node.getLiteralValue() + "\"" + node.getLiteralLanguage());
			else if (node.getLiteralDatatypeURI() != null)
				buffer.append("\"" + node.getLiteralLexicalForm() + "\"^^<" + node.getLiteralDatatypeURI() + ">");
			else
				buffer.append(node.toString());
		}
		if (node instanceof Node_NULL || node instanceof Node_ANY)
			buffer.append("?o");

		return buffer.toString();
	}
}
