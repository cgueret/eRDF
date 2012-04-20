package nl.erdf.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import nl.erdf.constraints.impl.StatementPatternConstraint;
import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.sparql.SPARQLDataLayer;
import nl.erdf.model.Directory;
import nl.erdf.model.Request;
import nl.erdf.model.Solution;
import nl.erdf.model.impl.StatementPatternProvider;
import nl.erdf.model.impl.Triple;
import nl.erdf.optimizer.Optimizer;
import nl.erdf.util.FileToText;
import nl.erdf.util.PatternsExtractor;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SPARQLEngine {
	// Logger
	static final Logger logger = LoggerFactory.getLogger(SPARQLEngine.class);

	// Timer for stopping the search
	static final Timer timer = new Timer("Timer", true);

	// Default maximum running time
	static final int MAX_TIME = 5;

	static class SearchProcess implements Observer {
		final SailRepository repository;
		final String sparqlQuery;
		final Optimizer optimizer;
		final Request request;
		final Directory directory;
		boolean stop = false;

		public SearchProcess(File queryFile, File endPoints) throws RepositoryException, FileNotFoundException,
				IOException {
			// Create a directory
			directory = new Directory();
			directory.loadFrom(new FileInputStream(endPoints));

			// Create a data layer
			DataLayer datalayer = new SPARQLDataLayer(directory);

			// Create an in-memory repository
			repository = new SailRepository(new MemoryStore());
			repository.initialize();

			// Store the query
			sparqlQuery = FileToText.convert(queryFile);

			// Initialise the request for eRDF
			request = new Request(datalayer);

			// Get the patterns out of the SPARQL query
			Set<StatementPattern> patterns = PatternsExtractor.fromSPARQL(sparqlQuery);
			for (StatementPattern pattern : patterns) {
				// Add the pattern to instantiate them in the solutions
				request.addStatementPattern(pattern);

				// Create a constraint
				request.addConstraint(new StatementPatternConstraint(pattern));

				// Use that pattern as a data source
				request.addResourceProvider(new StatementPatternProvider(pattern));
			}

			logger.info("Running eRDF with the following parameters:\n" + request.toString());
			optimizer = new Optimizer(datalayer, request, null);
			optimizer.addObserver(this);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Observer#update(java.util.Observable,
		 * java.lang.Object)
		 */
		public void update(Observable source, Object arg) {
			// Check source
			if (!(source instanceof Optimizer))
				return;

			// Get the solutions and their triples
			Set<Triple> triples = new HashSet<Triple>();
			@SuppressWarnings("unchecked")
			Collection<Solution> solutions = (Collection<Solution>) arg;
			for (Solution s : solutions)
				if (s.isOptimal())
					for (Triple triple : request.getTripleSet(s))
						triples.add(triple);

			// Add the triples
			if (triples.size() > 0) {
				logger.info("Found " + triples.size() + " relevant triples");
				for (Triple triple : triples) {
					logger.info(triple.toString());
					SailRepositoryConnection con = null;
					try {
						con = repository.getConnection();
						con.add(triple);
						con.commit();
					} catch (RepositoryException e) {
						e.printStackTrace();
					} finally {
						try {
							if (con != null)
								con.close();
						} catch (RepositoryException e) {
							e.printStackTrace();
						}
					}
				}
			}

			// If we should stop, do it
			if (stop) {
				optimizer.terminate();
				directory.close();

				// Execute the query and display results
				logger.info("Process query...");
				SailRepositoryConnection con = null;
				try {
					con = repository.getConnection();
					TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery);
					TupleQueryResult result = tupleQuery.evaluate();
					logger.info(result.toString());
				} catch (RepositoryException e) {
					e.printStackTrace();
				} catch (MalformedQueryException e) {
					e.printStackTrace();
				} catch (QueryEvaluationException e) {
					e.printStackTrace();
				} finally {
					try {
						if (con != null)
							con.close();
					} catch (RepositoryException e) {
						e.printStackTrace();
					}
				}

				// Print a list of informative sources
				logger.info("List of end points that provided information");
				for (EndPoint endPoint : directory.endPoints()) {
					if (endPoint.getInformativeCounter() > 0)
						logger.info(endPoint.getName() + " had results for " + endPoint.getInformativeCounter()
								+ " queries");
				}
			}
		}

		/**
		 * 
		 */
		public void start() {
			optimizer.run();
		}

		/**
		 * 
		 */
		public synchronized void stop() {
			stop = true;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Options options = new Options();

		// List of end points
		Option endpoints = new Option("s", "seeds", true, "location of the end points list");
		endpoints.setArgs(1);
		endpoints.setRequired(true);
		options.addOption(endpoints);

		// Query
		Option query = new Option("q", "query", true, "SPARQL query to execute");
		query.setArgs(1);
		query.setRequired(true);
		options.addOption(query);

		// Run time
		Option time = new Option("t", "time", true, "maximum execution time (default is " + MAX_TIME
				+ " minutes, < 0 means infinite)");
		query.setArgs(1);
		query.setOptionalArg(true);
		options.addOption(time);

		// Help
		Option help = new Option("h", "help", false, "print help");
		options.addOption(help);

		CommandLineParser parser = new BasicParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args, true);
			if (cmd.hasOption("h") || cmd.hasOption("help")) {
				formatter.printHelp(80, " ", "SPARQL querying over the Web of Linked Data\n", options,
						"\nLook at the README file for more information", true);
				System.exit(0);
			}

			if (!cmd.hasOption("s") || !cmd.hasOption("q")) {
				System.exit(-1);
			}

			run(cmd);
		} catch (org.apache.commons.cli.ParseException e) {
			formatter.printHelp(80, " ", "ERROR: " + e.getMessage() + "\n", options,
					"\nError occured! Please see the error message above", true);
			System.exit(-1);
		} catch (IOException e) {
			formatter.printHelp(80, " ", "ERROR: " + e.getMessage() + "\n", options,
					"\nError occured! Please see the error message above", true);
			System.exit(-1);
		} catch (URISyntaxException e) {
			formatter.printHelp(80, " ", "ERROR: " + e.getMessage() + "\n", options,
					"\nError occured! Please see the error message above", true);
			System.exit(-1);
		}

	}

	/**
	 * @param cmd
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws RepositoryException
	 */
	private static void run(CommandLine cmd) throws IOException, URISyntaxException, RepositoryException {
		// Check the query
		File queryFile = new File(cmd.getOptionValue("q"));
		if (!queryFile.exists())
			throw new FileNotFoundException("No file found at " + queryFile.getAbsolutePath());

		// Check the list of end points
		File endpoints = new File(cmd.getOptionValue("s"));
		if (!endpoints.exists())
			throw new FileNotFoundException("No file found at " + endpoints.getAbsolutePath());

		// Create a search process
		final SearchProcess p = new SearchProcess(queryFile, endpoints);

		// Schedule a timer to stop the query after some time
		int time = Integer.parseInt(cmd.getOptionValue("t", Integer.toString(MAX_TIME)));
		if (time > 0) {
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					logger.info("Time out, will stop the process as soon as possible");
					p.stop();
				}
			}, time * 60 * 1000);
		}

		// Start the search process
		p.start();
	}
}
