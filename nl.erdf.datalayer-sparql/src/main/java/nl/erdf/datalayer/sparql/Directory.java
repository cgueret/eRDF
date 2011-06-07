/**
 * 
 */
package nl.erdf.datalayer.sparql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author tolgam
 * 
 */
// Use
// http://download.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html
public class Directory {
	// Logger instance
	protected static final Logger logger = LoggerFactory.getLogger(Directory.class);

	// A list of SPARQL end points
	private LinkedList<EndPoint> listOfEndPoints = new LinkedList<EndPoint>();

	// The client connection manager with avoids DoS
	private final ClientConnectionManager connManager;

	// Connection parameters
	private final HttpParams httpParams;
	
	

	/**
	 * 
	 */
	public Directory() {
		// Create a scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

		// Set some parameters for the connections
		httpParams = new BasicHttpParams();
    	HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
    	HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
    	HttpProtocolParams.setUseExpectContinue(httpParams, true);
		HttpConnectionParams.setConnectionTimeout(httpParams, 200);
		httpParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 200);
		httpParams.setParameter(CoreConnectionPNames.TCP_NODELAY, true);
		httpParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 200);
		ConnManagerParams.setMaxTotalConnections(httpParams, 400);
		ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(2));
		//HttpParams params = httpClient.getParams();
		//HttpConnectionParams.setConnectionTimeout(params, 2000);
		//HttpConnectionParams.setSoTimeout(params, 2000);
		
		// Create a connection manager
		connManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

	}

	/**
	 * @return a copy of endPoints in that directory
	 */
	public Collection<EndPoint> endPoints() {
		synchronized (listOfEndPoints) {
			return listOfEndPoints;
		}
	}

	/**
	 * @param name
	 * @param URI
	 * @return the {@link EndPoint} loaded in the directory
	 */
	public EndPoint add(String name, String URI) {
		try {
			// Create and add the end point
			EndPoint endPoint = new EndPoint(name, URI);
			synchronized (listOfEndPoints) {
				listOfEndPoints.add(endPoint);
				endPoint.start(connManager, httpParams);
			}
			return endPoint;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Load the content of a directory from an input stream
	 * 
	 * @param input
	 *            the source to read from
	 */
	public synchronized void loadFrom(InputStream input) {
		// Shutdown the end points if they are running
		for (EndPoint endPoint : listOfEndPoints)
			endPoint.shutdown();

		// Clear the current list
		listOfEndPoints.clear();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			String line = reader.readLine();
			while (line != null) {
				if (!line.startsWith("#") && line.length() > 2) {
					String[] parts = line.split(";");
					this.add(parts[0], parts[1]);
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
		}

		logger.info("Loaded and started " + listOfEndPoints.size() + " end points");
	}

	/**
	 * Dump the content of the directory to an output stream
	 * 
	 * @param output
	 *            the output stream to write to
	 */
	public void writeTo(OutputStream output) {
		OutputStreamWriter writer = new OutputStreamWriter(output);
		for (EndPoint endPoint : listOfEndPoints) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(endPoint.getName()).append(";").append(endPoint.getURI().toString()).append("\n");
			try {
				writer.write(buffer.toString());
				writer.flush();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 
	 */
	public void close() {
		logger.info("Shutdown connections from the directory");
		for (EndPoint endPoint: listOfEndPoints)
			endPoint.shutdown();
		
		connManager.shutdown();
	}
}
