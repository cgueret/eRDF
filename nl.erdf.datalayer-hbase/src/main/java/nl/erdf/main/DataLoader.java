/**
 * 
 */
package nl.erdf.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.datalayer.hbase.NativeHBaseDataLayer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.openrdf.model.Statement;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class DataLoader implements RDFHandler, ParseErrorListener {
	// Logger
	protected static Logger logger = LoggerFactory.getLogger(DataLoader.class);

	// Data layer
	private final DataLayer datalayer;

	/**
	 * @param schemaName
	 * @throws IOException
	 */
	public DataLoader(DataLayer datalayer) throws IOException {
		this.datalayer = datalayer;
	}

	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws RDFParseException, RDFHandlerException, FileNotFoundException,
			IOException, ParseException {
		// Compose the options
		Options options = new Options();
		options.addOption("i", "input", true, "data input file (data.nt.bz2)");
		options.addOption("d", "dataset", true, "data set name");
		options.addOption("c", "command", true, "command (load, clear)");
		options.addOption("h", "help", false, "print help message");

		// Parse the command line
		CommandLineParser parser = new PosixParser();
		CommandLine line = parser.parse(options, args);

		// Handle request for help
		if (line.hasOption("h"))
			printHelpAndExit(options, 0);

		// Handle miss-use
		if (!line.hasOption("c") || !line.hasOption("d"))
			printHelpAndExit(options, -1);

		// Create an instance of the data loader
		DataLayer dataLayer = NativeHBaseDataLayer.getInstance(line.getOptionValue("d"));
		DataLoader dataLoader = new DataLoader(dataLayer);

		// Handle commands
		if (line.getOptionValue("c").equals("clear")) {
			// Clear
			logger.info("Clearing data set " + line.getOptionValue("d"));
			dataLayer.clear();
			dataLayer.shutdown();
		} else if (line.getOptionValue("c").equals("load")) {
			// Load
			logger.info("Load " + line.getOptionValue("i") + " into data set " + line.getOptionValue("d"));
			dataLoader.load(line.getOptionValue("i"));
			dataLayer.shutdown();
		} else {
			// Unknown command
			logger.info("Unknown command " + line.getOptionValue("c"));
			printHelpAndExit(options, 0);
		}
	}

	/**
	 * @param exitCode
	 */
	public static void printHelpAndExit(Options options, int exitCode) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(DataLoader.class.getName(), options);
		System.exit(exitCode);
	}

	/**
	 * @param string
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 */
	public void load(String fileName) throws FileNotFoundException {
		logger.info("Start loading " + fileName);
		FileInputStream input = new FileInputStream(fileName);
		RDFParser parser = new NTriplesParser();
		parser.setRDFHandler(this);
		parser.setStopAtFirstError(false);
		parser.setVerifyData(false);
		parser.setParseErrorListener(this);
		try {
			parser.parse(new BZip2CompressorInputStream(input), "http://dbpedia.org");
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("End");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#startRDF()
	 */
	public void startRDF() throws RDFHandlerException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#endRDF()
	 */
	public void endRDF() throws RDFHandlerException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#handleNamespace(java.lang.String,
	 * java.lang.String)
	 */
	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.rio.RDFHandler#handleStatement(org.openrdf.model.Statement)
	 */
	public void handleStatement(Statement st) throws RDFHandlerException {
		logger.info(st.toString());
		datalayer.add(st);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#handleComment(java.lang.String)
	 */
	public void handleComment(String comment) throws RDFHandlerException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.ParseErrorListener#warning(java.lang.String, int,
	 * int)
	 */
	public void warning(String msg, int lineNo, int colNo) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.ParseErrorListener#error(java.lang.String, int, int)
	 */
	public void error(String msg, int lineNo, int colNo) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.ParseErrorListener#fatalError(java.lang.String, int,
	 * int)
	 */
	public void fatalError(String msg, int lineNo, int colNo) {
		// TODO Auto-generated method stub

	}

}
