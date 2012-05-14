/**
 * 
 */
package nl.erdf.main;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import nl.erdf.datalayer.hbase.NativeHBaseDataLayer;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.ntriples.NTriplesParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class DataLoader implements RDFHandler {
	// Logger
	protected static Logger logger = LoggerFactory.getLogger(DataLoader.class);

	private final NativeHBaseDataLayer datalayer;

	/**
	 * @throws IOException
	 */
	public DataLoader() throws IOException {
		datalayer = new NativeHBaseDataLayer();
	}

	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 */
	public static void main(String[] args) throws RDFParseException, RDFHandlerException, FileNotFoundException,
			IOException {
		DataLoader d = new DataLoader();
		if (args[0].equals("clean"))
			d.datalayer.clear();
		else
			d.load(args[0]);
	}

	/**
	 * @param string
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 */
	private void load(String fileName) throws RDFParseException, RDFHandlerException, FileNotFoundException,
			IOException {
		logger.info("Start " + fileName);
		NTriplesParserFactory f = new NTriplesParserFactory();
		RDFParser parser = f.getParser();
		parser.setRDFHandler(this);
		parser.parse(new BZip2CompressorInputStream(new FileInputStream(fileName)), "http://dbpedia.org");
		logger.info("End");
		datalayer.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#startRDF()
	 */
	public void startRDF() throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#endRDF()
	 */
	public void endRDF() throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#handleNamespace(java.lang.String,
	 * java.lang.String)
	 */
	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openrdf.rio.RDFHandler#handleStatement(org.openrdf.model.Statement)
	 */
	public void handleStatement(Statement st) throws RDFHandlerException {
		System.out.println(st);
		datalayer.add(st);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openrdf.rio.RDFHandler#handleComment(java.lang.String)
	 */
	public void handleComment(String comment) throws RDFHandlerException {
		// TODO Auto-generated method stub

	}

}
