/**
 * 
 */
package nl.erdf.datalayer.sparql.ng;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.main.DataSource;


/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SPARQLDataLayerNG extends Observable implements DataLayer {
	// Logger instance
	final Logger logger = LoggerFactory.getLogger(SPARQLDataLayerNG.class);

	// Triple store for the cache
	final TripleStore triplesCache = new JenaTDBStore();

	// List of data sources to query for new triples
	final List<DataSource> dataSources = new ArrayList<DataSource>();
	
	// Lock
	final ReentrantLock lock = new ReentrantLock();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#isValid(com.hp.hpl.jena.graph.Triple)
	 */
	public boolean isValid(Triple triple) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#clear()
	 */
	public void clear() {
		triplesCache.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#shutdown()
	 */
	public void shutdown() {
		triplesCache.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#waitForLatencyBuffer()
	 */
	public void waitForLatencyBuffer() {
	}

	/* (non-Javadoc)
	 * @see nl.erdf.datalayer.DataLayer#getNumberOfResources(com.hp.hpl.jena.graph.Triple)
	 */
	public long getNumberOfResources(Triple triple) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see nl.erdf.datalayer.DataLayer#getResources(com.hp.hpl.jena.graph.Triple)
	 */
	public Set<Node> getResources(Triple triple) {
		return null;
	}

	/**
	 * @param dataSource
	 */
	public void addDataSource(DataSource dataSource) {
		dataSources.add(dataSource);
	}

}
