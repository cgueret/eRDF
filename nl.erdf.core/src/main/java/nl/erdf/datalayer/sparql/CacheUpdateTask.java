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

	// Is it a cancelled job?
	private boolean isCancelled = false;

	/**
	 * @param httpClient
	 * @param params
	 * @param cm
	 * @param endpoints
	 * @param queryPattern
	 * @param resourceSet
	 *           The result to update
	 */
	public CacheUpdateTask(EndPoint endpoint, NodeSet resourceSet) {
		this.endpoint = endpoint;
		this.resourceSet = resourceSet;
	}

	/**
	 * Parse the result from a SPARQL query
	 */
	private class Handler extends DefaultHandler {
		// Lock system
		private boolean isFinished = false;
		private ReentrantLock finishLock = new ReentrantLock();
		private Condition finished = finishLock.newCondition();

		// The set of resources parsed
		private final NodeSet resources;

		// Tracks how many resources have been parsed
		private int total = 0;

		private boolean inBind = false;
		private Node node = null;
		private String literType = null;
		private String literLang = null;
		private StringBuffer buffer = new StringBuffer();

		// private List<Node> resultSet = new ArrayList<Node>();

		/**
		 * @param source
		 *           the address of the sparql endpoint
		 * @param results
		 */
		public Handler(EndPoint source, NodeSet resources) {
			this.resources = resources;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
		 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qname, Attributes attr) {
			if (qname.equals("uri")) {
				inBind = true;
			} else if (qname.equals("bnode")) {
				inBind = true;
			} else if (qname.equals("literal")) {
				inBind = true;
				literType = attr.getValue("datatype");
				literLang = attr.getValue("xml:lang");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		@Override
		public void characters(char[] ch, int start, int length) {
			if (!inBind) {
				return;
			}
			String s = new String(ch, start, length);
			if (s.trim().length() == 0) {
				return;
			}
			buffer.append(s);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
		 * java.lang.String, java.lang.String)
		 */
		@Override
		public void endElement(String uri, String localName, String qname) {
			if (qname.equals("result")) {
				if (node != null) {
					resources.add(node);
					total++;
					node = null;
				}
			} else if (qname.equals("uri")) {
				node = Node.createURI(buffer.toString());
				buffer.delete(0, buffer.length());
			} else if (qname.equals("bnode")) {
				// FIXME We ignore the blank nodes for the moment
				buffer.delete(0, buffer.length());
			} else if (qname.equals("literal")) {
				node = Node.createLiteral(buffer.toString(), literLang, TypeMapper.getInstance().getTypeByName(literType));
				buffer.delete(0, buffer.length());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
		 */
		@Override
		public void endDocument() {
			finishLock.lock();
			try {
				isFinished = true;
				finished.signalAll();
			} finally {
				finishLock.unlock();
			}
		}

		/**
		 * @return the number of items that have been added to the set
		 * 
		 */
		public int waitForCompletion() {
			// Wait for completion
			finishLock.lock();
			try {
				while (!isFinished)
					finished.await();
			} catch (InterruptedException ie) {
			} finally {
				finishLock.unlock();
			}

			// Return the number of resources found
			return total;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			// If cancelled, return right away
			if (isCancelled)
				return;

			QueryPattern pattern = resourceSet.getPattern();

			// Don't test if a blank node not issued by that peer is used
			/*
			 * for (Resource node : pattern.nodes()) if (node instanceof BNode) if
			 * (!((BNode) node).getSource().equals(endpoint)) return;
			 */

			HttpGet httpget = null;
			HttpEntity entity = null;
			String uri = null;
			try {
				// Get the query
				String select = "SELECT " + QueryPattern.RETURN + " WHERE { ";
				select += pattern.toQueryString();
				select += ". } LIMIT 1000";
				String query = URLEncoder.encode(select, "UTF-8");
				uri = endpoint.getURI() + "?query=" + query;
				//logger.info(""+select);
				
				// Record the request
				endpoint.setRequestsCounter(endpoint.getRequestsCounter() + 1);

				// Open the connection
				httpget = new HttpGet(uri);
				httpget.addHeader("Accept", "application/sparql-results+xml");

				// Get current time
				long start = System.nanoTime();

				// Execute the request
				HttpResponse response = endpoint.getHttpClient().execute(httpget);
				entity = response.getEntity();

				if (entity != null) {
					// Get the results
					InputStream instream = entity.getContent();
					Handler handler = new Handler(endpoint, resourceSet);

					// Get a parser for the results
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser saxParser = factory.newSAXParser();
					saxParser.parse(instream, handler);

					// Wait for completion
					int total = handler.waitForCompletion();
					entity.consumeContent();

					// Update statistics
					long latency = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
					endpoint.setTotalLatency(endpoint.getTotalLatency() + latency);
					if (total > 0)
						endpoint.setInformativeCounter(endpoint.getInformativeCounter() + 1);
				} else {
					if (httpget != null)
						httpget.abort();
				}
				// httpClient.getConnectionManager().releaseConnection(conn, -1,
				// null);

			} catch (Exception e) {
				// There was an error when asking the provider
				endpoint.setErrorsCounter(endpoint.getErrorsCounter() + 1);
				//logger.error("Failed to query " + endpoint.getName() + " for " + pattern);
				if (httpget != null)
					httpget.abort();
			}
		} finally {
			// logger.info(endpoint.getName() + " " + pattern);
			resourceSet.decreaseUpdateTasksCounter();
		}
	}

	/**
	 * 
	 */
	public synchronized void cancel() {
		isCancelled = true;
	}
}
