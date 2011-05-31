package nl.erdf.datalayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class QueryPattern {
	protected final static Logger logger = LoggerFactory.getLogger(QueryPattern.class);

	/** Value used to get a result */
	public final static Node RETURN = Node.createVariable("value");

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
	public boolean contains(Node o) {
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
	 * Return the triple pattern wrapped by this QueryPattern
	 * 
	 * @return Triple the triple pattern
	 */
	public Triple getPattern() {
		return pattern;
	}
}
