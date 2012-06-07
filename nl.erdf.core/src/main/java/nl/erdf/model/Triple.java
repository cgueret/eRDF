/**
 * 
 */
package nl.erdf.model;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Triple implements Statement {
	// Serial
	private static final long serialVersionUID = -3001476772317067797L;

	// The statement's subject.
	private final Resource subject;

	// The statement's predicate.
	private final URI predicate;

	// The statement's object.
	private final Value object;

	/**
	 * Creates a new Statement with the supplied subject, predicate and object.
	 * At most one <tt>null</tt> value is allowed
	 * 
	 * @param subject
	 *            The statement's subject.
	 * @param predicate
	 *            The statement's predicate.
	 * @param object
	 *            The statement's object.
	 */
	public Triple(Resource subject, URI predicate, Value object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;

		assert (getNumberNulls() < 1);
	}

	/**
	 * @return the number of nulls in the triple
	 */
	public int getNumberNulls() {
		int nulls = 0;
		nulls += (subject == null ? 1 : 0);
		nulls += (predicate == null ? 1 : 0);
		nulls += (object == null ? 1 : 0);
		return nulls;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.model.Statement#getSubject()
	 */
	public Resource getSubject() {
		return subject;
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
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
		if (getClass() != obj.getClass())
			return false;
		Triple other = (Triple) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;

		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;

		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.model.Statement#getPredicate()
	 */
	public URI getPredicate() {
		return predicate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.model.Statement#getObject()
	 */
	public Value getObject() {
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.model.Statement#getContext()
	 */
	public Resource getContext() {
		// Not applicable
		return null;
	}

	/**
	 * Gives a String-representation of this Statement that can be used for
	 * debugging.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(256);

		sb.append("(");
		sb.append(getSubject());
		sb.append(", ");
		sb.append(getPredicate());
		sb.append(", ");
		sb.append(getObject());
		sb.append(")");

		return sb.toString();
	}

}
