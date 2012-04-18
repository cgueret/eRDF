/**
 * 
 */
package nl.erdf.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class TripleSet {
	private final Set<Triple> triples = new HashSet<Triple>();

	/**
	 * @param t
	 * @return true if the triple is in the set
	 */
	public boolean contains(Triple t) {
		return triples.contains(t);
	}

	/**
	 * @param tripleSet
	 */
	public void addAll(Set<Triple> tripleSet) {
		triples.addAll(tripleSet);
	}

}
