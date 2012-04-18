/**
 * 
 */
package nl.erdf.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import nl.erdf.model.EndPoint.EndPointType;
import nl.erdf.vocabulary.SD;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

/**
 * A directory is a collection of end points
 * 
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class Directory extends ArrayList<EndPoint> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7897336454073821165L;

	/**
	 * @param fileName
	 * @return
	 */
	public static Directory create(String fileName) {
		Directory directory = new Directory();

		try {
			// Init repository
			SailRepository repository = new SailRepository(new MemoryStore());
			repository.initialize();

			// Load file
			SailRepositoryConnection connection = repository.getConnection();
			connection.add(new File(fileName), "http://example.com/", RDFFormat.TURTLE);

			// Extract end points
			for (Statement stmt : connection.getStatements(null, RDF.TYPE, SD.SERVICE, true).asList()) {
				Resource entry = stmt.getSubject();
				List<Statement> endpointStmts = connection.getStatements(entry, SD.ENDPOINT, null, true).asList();
				List<Statement> graphStmts = connection.getStatements(entry, SD.DEFAULT_GRAPH, null, true).asList();
				List<Statement> langStmts = connection.getStatements(entry, SD.SUPPORTED_LANGUAGE, null, true).asList();
				if (endpointStmts.size() != 1 || langStmts.size() != 1)
					throw new Exception("Error reading data for the end point, missing information");

				String endpoint = endpointStmts.get(0).getObject().stringValue();
				String defaultGraph = (graphStmts.size() == 1 ? graphStmts.get(0).getObject().stringValue() : null);
				String lang = langStmts.get(0).getObject().stringValue();
				if (lang.equals("virtuoso"))
					directory.add(new EndPoint(endpoint, defaultGraph, EndPointType.VIRTUOSO));
				else if (lang.equals("owlim"))
					directory.add(new EndPoint(endpoint, defaultGraph, EndPointType.OWLIM));
				else
					throw new Exception("Unknown language");
			}

			// Free memory and close connection
			connection.clear();
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return directory;
	}
}
