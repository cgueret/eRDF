package nl.erdf.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Observable;
import java.util.Observer;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.sparql.Directory;
import nl.erdf.datalayer.sparql.SPARQLDataLayer;
import nl.erdf.model.Solution;
import nl.erdf.model.wod.SPARQLParser;
import nl.erdf.model.wod.SPARQLRequest;
import nl.erdf.optimizer.Optimizer;
import nl.erdf.util.FileToText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 *
 */
public class SPARQLEngine {
	static final Logger logger = LoggerFactory.getLogger(SPARQLEngine.class);

	/**
	 * @author cgueret
	 * 
	 */
	static class Process implements Observer {
		final Model model;
		final Query query;
		final Optimizer optimizer;
		final SPARQLRequest request;

		public Process(String q) throws IOException, URISyntaxException {
			// Get the queryFile
			File queryFile = new File(q);

			// Create a data layer
			Directory directory = new Directory();
			directory.loadFrom(new FileInputStream("data/dogfood_dbpedia.csv"));
			//directory.add("sp2b", "http://127.0.0.1:10000/sparql/");
			DataLayer datalayer = new SPARQLDataLayer(directory);

			// Create a model for Jena, load the query
			model = ModelFactory.createMemModelMaker().createDefaultModel();
			query = QueryFactory.create(FileToText.convert(queryFile));

			// Put some data in
			// logger.info("Load data...");
			// InputStream in = new FileInputStream(new
			// File("/home/cgueret/tmp/sp2b/100k.rdf"));
			// model.read(in,null);
			// in.close();

			// Init eRDF
			request = new SPARQLRequest(datalayer);
			SPARQLParser parser = new SPARQLParser(request);
			parser.parseFromFile(queryFile);
			logger.info(request.toString());
			optimizer = new Optimizer(datalayer, request, null);
			optimizer.addObserver(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
		 */
		@Override
		public void update(Observable source, Object arg) {
			// Check source
			if (!(source instanceof Optimizer))
				return;

			// Ignore non optimal solutions
			Solution best = (Solution) arg;
			logger.info(best.toString());
			if (!best.isOptimal())
				return;

			int count = 0;
			for (Triple triple : request.getTripleSet(best).getTriples()) {
				logger.info("Add " + triple);
				model.add(model.asStatement(triple));
				count++;
			}
			// logger.info("Added " + count);

			// Execute the query and display results
			logger.info("Process query...");
			QueryExecution qe = QueryExecutionFactory.create(query, model);
			if (query.isSelectType()) {
				ResultSet results = qe.execSelect();
				ResultSetFormatter.out(System.out, results, query);
				qe.close();
			}
			else if (query.isAskType()) {
				boolean result = qe.execAsk();
				ResultSetFormatter.out(System.out, result);
				qe.close();
			}
		}

		public void start() {
			optimizer.run();
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {
		Process p = new Process("data/people.sparql");//"data/sp2bench/q02.sparql"
		p.start();
	}
}
