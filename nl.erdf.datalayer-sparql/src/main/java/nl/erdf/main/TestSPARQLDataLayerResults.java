/**
 * 
 */
package nl.erdf.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import nl.erdf.constraints.impl.StatementPatternConstraint;
import nl.erdf.datalayer.sparql.SPARQLDataLayer;
import nl.erdf.model.Directory;
import nl.erdf.model.EndPoint;
import nl.erdf.model.EndPoint.EndPointType;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;
import nl.erdf.model.Triple;
import nl.erdf.model.impl.StatementPatternProvider;
import nl.erdf.optimizer.Optimizer;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class TestSPARQLDataLayerResults {
	static final Logger logger = LoggerFactory
			.getLogger(TestSPARQLDataLayerResults.class);
	private final Directory directory;
	private final SPARQLDataLayer datalayer;

	public TestSPARQLDataLayerResults() {
		// Create a directory
		directory = new Directory();
		directory.add(new EndPoint("http://dbpedia.org/sparql",
				"http://dbpedia.org", EndPointType.VIRTUOSO));

		// Create a data layer
		datalayer = new SPARQLDataLayer(directory);

		Triple t = new Triple(null, new URIImpl(
				"http://dbpedia.org/ontology/wikiPageExternalLink"), null);
		logger.info(datalayer.isValid(t) ? "ok" : "nok");
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		TestSPARQLDataLayerResults test = new TestSPARQLDataLayerResults();
	}

}
