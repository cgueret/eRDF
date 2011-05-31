/**
 * 
 */
package nl.erdf.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.erdf.constraints.TripleConstraint;
import nl.erdf.datalayer.sparql.Directory;
import nl.erdf.datalayer.sparql.SPARQLDataLayer;
import nl.erdf.datalayer.sparql.SPARQLRequest;
import nl.erdf.model.Solution;
import nl.erdf.optimizer.Optimizer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Test implements Observer {
	static final Logger logger = LoggerFactory.getLogger(Test.class);
	private final Optimizer optimizer;
	private final SPARQLRequest request;
	private final Directory directory;

	public Test() throws FileNotFoundException, IOException {
		// Create a directory
		directory = new Directory();
		directory.add("DBPedia", "http://dbpedia.org/sparql");

		// Create a data layer
		SPARQLDataLayer datalayer = new SPARQLDataLayer(directory);

		// Create the request
		request = new SPARQLRequest(datalayer);
		Triple birthPlace = Triple.create(Node.createVariable("person"),Node.createURI("http://dbpedia.org/ontology/birthPlace"), Node.createURI("http://dbpedia.org/resource/Netherlands"));
		request.addConstraint(new TripleConstraint(birthPlace));
		Triple artist = Triple.create(Node.createVariable("person"),Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), Node.createURI("http://dbpedia.org/ontology/Artist"));
		request.addConstraint(new TripleConstraint(artist));
		
		// Create the optimiser
		optimizer = new Optimizer(datalayer, request, null);
		optimizer.addObserver(this);
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Test test = new Test();
		test.run();
	}

	/**
	 * 
	 */
	private void run() {
		optimizer.run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable source, Object arg) {
		// Check source
		if (!(source instanceof Optimizer))
			return;

		// Get the best solution
		@SuppressWarnings("unchecked")
		Collection<Solution> solutions = (Collection<Solution>) arg;
		boolean stop = false;
		for (Solution s : solutions) {
			if (s.isOptimal()) {
				logger.info("Found optimal solution:");
				Model model = ModelFactory.createDefaultModel();
				for (Triple triple : request.getTripleSet(s))
					logger.info(model.asStatement(triple).toString());
				stop = true;
			}
		}

		// If we should stop, do it
		if (stop) {
			optimizer.terminate();
			directory.close();
		}
	}

}
