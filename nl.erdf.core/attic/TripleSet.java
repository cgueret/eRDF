package nl.erdf.model.wod;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Christophe Gueret
 * 
 */
public class TripleSet {
	// The set of triples
	private Set<Triple> triples = Collections
			.synchronizedSet(new HashSet<Triple>());

	/**
	 * @param subject
	 * @param predicate
	 * @param object
	 */
	public void add(Node subject, Node predicate, Node object) {
		add(Triple.create(subject, predicate, object));
	}

	/**
	 * @param triple
	 */
	public void add(Triple triple) {
		synchronized (triples) {
			triples.add(triple);
		}
	}

	/**
	 * @param tripleSet
	 */
	public void addAll(TripleSet tripleSet) {
		for (Triple triple : tripleSet.triples)
			add(triple);
	}

	/**
	 * @param triple
	 * @return true if the set contains the triple
	 */
	public boolean contains(Triple triple) {
		boolean res = false;
		synchronized (triples) {
			res = triples.contains(triple);
		}
		return res;
	}

	/**
	 * @param other
	 * @return the overlap ratio with the other set
	 */
	public double overlapWith(TripleSet other) {
		// Find who is the biggest
		Set<Triple> smallSet = other.triples;
		Set<Triple> bigSet = triples;
		if (smallSet.size() > bigSet.size()) {
			smallSet = triples;
			bigSet = other.triples;
		}

		// If nothing to compare, return 0;
		if (smallSet.size() == 0)
			return 0;

		// Count the number of overlapping triples
		double overlap = 0;
		for (Triple triple : bigSet)
			if (smallSet.contains(triple))
				overlap++;
		overlap = overlap / smallSet.size();

		// Return the normalized value
		return overlap;
	}

	/**
	 * @return the triples
	 */
	public Set<Triple> getTriples() {
		return triples;
	}

}
