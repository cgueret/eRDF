/**
 * 
 */
package nl.erdf.main;

import java.io.File;
import java.io.FileInputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;

import nl.erdf.constraints.impl.StatementPatternConstraint;
import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.impl.MemoryDataLayer;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;
import nl.erdf.model.impl.StatementPatternProvider;
import nl.erdf.optimizer.Optimizer;
import nl.erdf.util.FileToText;
import nl.erdf.util.PatternsExtractor;

import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.turtle.TurtleParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SimpleTest implements Observer {
	/** Logger */
	protected final Logger logger = LoggerFactory.getLogger(SimpleTest.class);

	private final Optimizer optimizer;

	/**
	 * @param dataFile
	 * @param queryFile
	 * @throws Exception
	 */
	public SimpleTest(String dataFile, String queryFile) throws Exception {
		// Create a simple in memory data layer
		DataLayer dataLayer = new MemoryDataLayer();

		// Load some data
		MyHandler handler = new MyHandler(dataLayer);
		TurtleParserFactory f = new TurtleParserFactory();
		RDFParser p = f.getParser();
		p.setRDFHandler(handler);
		p.parse(new FileInputStream(dataFile), new String("http://example.com"));

		// Get the patterns out of the SPARQL query
		String queryStr = FileToText.convert(new File(queryFile));
		Set<StatementPattern> patterns = PatternsExtractor.fromSPARQL(queryStr);

		Request request = new Request(dataLayer);
		for (StatementPattern pattern : patterns) {
			// Add the pattern to instantiate them in the solutions
			request.addStatementPattern(pattern);

			// Create a constraint
			request.addConstraint(new StatementPatternConstraint(pattern));

			// Use that pattern as a data source
			request.addResourceProvider(new StatementPatternProvider(pattern));
		}

		// Create the optimizer
		optimizer = new Optimizer(dataLayer, request, null);
		optimizer.addObserver(this);

	}

	/**
	 * 
	 */
	public void run() {
		optimizer.run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public void update(Observable arg0, Object arg1) {
		if (!(arg1 instanceof SortedSet<?>))
			return;
		SortedSet<Solution> population = (SortedSet<Solution>) arg1;

		// optimizer.terminate();
		for (Solution s : population) {
			if (s.getFitness() == 1) {
				logger.info(s.toString());
				optimizer.terminate();
			}
		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String data = "data/lubm/data/University0_0.ttl";
		String query = "data/lubm/q4.rq";
		SimpleTest main = new SimpleTest(data, query);
		main.run();
	}
}
