/**
 * 
 */
package nl.erdf.main;

import java.util.Set;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SPARQLEndPoint implements DataSource {
	final String URI;
	final String name;

	/**
	 * @param name
	 * @param URI
	 */
	public SPARQLEndPoint(String name, String URI) {
		this.name = name;
		this.URI = URI;
	}

	/**
	 * @param query
	 * @return
	 */
	public Set<Statement> getStatements(Triple query) {
		// Query query = QueryFactory.create(queryString);
		// QueryExecution qexec = QueryExecutionFactory.sparqlService(URI,
		// query);
		// ResultSet resultSet = qexec.execSelect();
		return null;
	}

}
