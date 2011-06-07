/**
 * 
 */
package nl.erdf.datalayer.sparql.ng;

import java.util.Set;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public interface TripleStore {
	/**
	 * @param triple
	 * @return a set of resources matching the triple
	 */
	abstract public Set<Triple> getTriples(Triple triple);
	
	/**
	 * @param triple
	 */
	abstract void addTriple(Triple triple);
	
	/**
	 * 
	 */
	abstract void shutdown();
	
	/**
	 * 
	 */
	abstract void clear();
	
}
