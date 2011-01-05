/**
 * 
 */
package nl.erdf.datalayer.sparql;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nl.erdf.datalayer.QueryPattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_NULL;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * @author tolgam
 */
public class CacheUpdateTask implements Runnable {
	// Logging stuff
	protected final static Logger logger = LoggerFactory.getLogger(CacheUpdateTask.class);

	// ResourceSet to update with the results
	private final NodeSet resourceSet;

	// The end point to query
	private final EndPoint endpoint;

	// Is it a canceled job?
	private boolean isCanceled = false;

	// Delimiter to source the blank nodes
	protected final static String BNODE_SRC_MARKER = "####";

	/**
	 * @param endpoint
	 *            The endpoint to query
	 * @param resourceSet
	 *            The result to update
	 */
	public CacheUpdateTask(EndPoint endpoint, NodeSet resourceSet) {
		this.endpoint = endpoint;
		this.resourceSet = resourceSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			// If canceled, return right away
			if (isCanceled)
				return;

			// Get the query pattern
			QueryPattern pattern = resourceSet.getPattern();

			// Don't query if a blank node not issued by that peer is in use
			String srcid = Integer.toString(endpoint.hashCode());
			Triple triple = pattern.getPattern();
			if (triple.getSubject() instanceof Node_Blank)
				if (!triple.getSubject().getBlankNodeLabel().split(BNODE_SRC_MARKER)[1].equals(srcid))
					return;
			if (triple.getPredicate() instanceof Node_Blank)
				if (!triple.getPredicate().getBlankNodeLabel().split(BNODE_SRC_MARKER)[1].equals(srcid))
					return;
			if (triple.getObject() instanceof Node_Blank)
				if (!triple.getObject().getBlankNodeLabel().split(BNODE_SRC_MARKER)[1].equals(srcid))
					return;

			// Record the request
			endpoint.setRequestsCounter(endpoint.getRequestsCounter() + 1);

			// Generate the query
			String queryStr = queryPatternToSPARQLSelect(pattern);

			// Get current time
			long start = System.nanoTime();

			try {
				// Run the query
				Query query = QueryFactory.create(queryStr);
				QueryEngineHTTP qexec = QueryExecutionFactory.createServiceRequest(endpoint.getURI().toString(), query);
				ResultSet results = qexec.execSelect();

				// Update statistics
				long latency = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
				endpoint.setTotalLatency(endpoint.getTotalLatency() + latency);

				if (results.hasNext()) {
					// Update statistics
					endpoint.setInformativeCounter(endpoint.getInformativeCounter() + 1);

					// Use the results
					while (results.hasNext()) {
						QuerySolution res = results.next();
						Node n = res.get(QueryPattern.RETURN.getName()).asNode();
						if (n.isBlank()) {
							logger.info(n.toString() + " " + queryStr);
							ResultSetFormatter.out(System.out, results, query);
							resourceSet
									.add(Node.createAnon(new AnonId(n.getBlankNodeLabel() + BNODE_SRC_MARKER + srcid)));
						} else {
							resourceSet.add(n);
						}
					}
				}
				// httpClient.getConnectionManager().releaseConnection(conn, -1,
				// null);
			} catch (Exception e) {
				// There was an error when asking the provider
				endpoint.setErrorsCounter(endpoint.getErrorsCounter() + 1);
				logger.error("Failed to query " + endpoint.getName() + " for " + queryStr);
			} finally {
			}
		} finally {
			// logger.info(endpoint.getName() + " " + pattern);
			resourceSet.decreaseUpdateTasksCounter();
		}
	}

	/**
	 * @param pattern
	 * @return
	 */
	private String queryPatternToSPARQLSelect(QueryPattern queryPattern) {
		StringBuffer buffer = new StringBuffer();
		Triple pattern = queryPattern.getPattern();

		// Open
		buffer.append("SELECT ").append(QueryPattern.RETURN).append(" WHERE {");

		// Convert
		String s = nodeToString(pattern.getSubject());
		buffer.append((s.equals("") ? "?s" : s)).append(" ");
		String p = nodeToString(pattern.getPredicate());
		buffer.append((p.equals("") ? "?p" : p)).append(" ");
		String o = nodeToString(pattern.getObject());
		buffer.append((o.equals("") ? "?o" : o)).append(".");

		// Close
		buffer.append("} LIMIT 1000");

		return buffer.toString();
	}

	/**
	 * @param node
	 */
	private String nodeToString(Node node) {
		// Handle URIs
		if (node instanceof Node_URI)
			return "<" + node.getURI() + ">";

		// Handle Variables
		if (node instanceof Node_Variable)
			return node.toString();

		// Handle Literals
		if (node instanceof Node_Literal) {
			if (!node.getLiteralLanguage().equals(""))
				return "\"" + node.getLiteralValue() + "\"" + node.getLiteralLanguage();
			if (node.getLiteralDatatypeURI() != null)
				return "\"" + node.getLiteralLexicalForm() + "\"^^<" + node.getLiteralDatatypeURI() + ">";
			return node.toString();
		}

		// Handle Blanks
		if (node instanceof Node_Blank) {
			String[] blocks = node.getBlankNodeLabel().split(BNODE_SRC_MARKER);
			return "<" + blocks[0] + ">";
		}
		if (node instanceof Node_NULL || node instanceof Node_ANY)
			return "";

		logger.info(node.getClass().toString());
		return null;
	}

	/**
	 * 
	 */
	public synchronized void cancel() {
		isCanceled = true;
	}
}
