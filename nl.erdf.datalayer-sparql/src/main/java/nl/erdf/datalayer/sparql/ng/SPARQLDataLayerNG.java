/**
 * 
 */
package nl.erdf.datalayer.sparql.ng;

import java.util.Observable;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.QueryPattern;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SPARQLDataLayerNG extends Observable implements DataLayer {
	// Logger instance
	final Logger logger = LoggerFactory.getLogger(SPARQLDataLayerNG.class);

	// Triple store for the cache
	final TripleStore triplesCache = new JenaTDBStore();

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#getNumberOfResources(nl.erdf.datalayer.
	 * QueryPattern)
	 */
	@Override
	public long getNumberOfResources(QueryPattern queryPattern) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#getRandomResource(java.util.Random,
	 * nl.erdf.datalayer.QueryPattern)
	 */
	@Override
	public Node getRandomResource(Random random, QueryPattern queryPattern) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#isValid(com.hp.hpl.jena.graph.Triple)
	 */
	@Override
	public boolean isValid(Triple triple) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#clear()
	 */
	@Override
	public void clear() {
		triplesCache.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#shutdown()
	 */
	@Override
	public void shutdown() {
		triplesCache.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#waitForLatencyBuffer()
	 */
	@Override
	public void waitForLatencyBuffer() {
		// TODO Auto-generated method stub

	}

}
