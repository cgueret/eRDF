/**
 * 
 */
package nl.erdf.datalayer.sparql;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

/**
 * @author tolgam
 * 
 */
public class EndPoint {
	// Logger instance
	static final Logger logger = LoggerFactory.getLogger(EndPoint.class);

	// The Ping query used to check if an end point is alive
	static final Query query = QueryFactory
			.create("SELECT * WHERE {?s ?p ?o} LIMIT 1");

	// The name of this end point
	private final String name;

	// The URI poiting to it
	private final URI URI;

	// Parameters for the connections to SPARQL end points
	private final HttpParams httpParams;

	// The client connection manager with avoids DoS
	private ClientConnectionManager connManager;

	// Executor to run the update tasks against this end point
	private ExecutorService executor;
	private LinkedBlockingQueue<Runnable> jobQueue;

	// The http client used to connect to the end point
	private DefaultHttpClient httpClient;

	// Number of requests sent to the endpoint
	private long requestsCounter = 0;

	// Total latency
	private long totalLatency = 0;

	// Number of failed requests
	private long errorsCounter = 0;

	// Number of queries that returned some results
	private int informativeCounter = 0;

	// By default, the end point is not enabled
	private boolean enabled = false;

	/**
	 * @param name
	 * @param address
	 * @throws URISyntaxException
	 */
	public EndPoint(final String name, final String address)
			throws URISyntaxException {
		this.name = name;
		this.URI = new URI(address);

		httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 500);
		// ConnManagerParams.setTimeout(httpParams, 5000);
		ConnManagerParams.setMaxTotalConnections(httpParams, 200);
		ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
				new ConnPerRouteBean(2));
	}

	/**
	 * Clear the statistics
	 */
	public void clear() {
		// Reset the stats
		requestsCounter = 0;
		totalLatency = 0;
		errorsCounter = 0;
		informativeCounter = 0;

		// Cancel remaining tasks
		for (Runnable runnable : jobQueue)
			if (runnable instanceof CacheUpdateTask)
				((CacheUpdateTask) runnable).cancel();
		jobQueue.clear();

		// Wait for the running ones to be stopped
		((ThreadPoolExecutor) executor).prestartAllCoreThreads();
		try {
			while (((ThreadPoolExecutor) executor).getActiveCount() != 0)
				Thread.sleep(100);
		} catch (InterruptedException ie) {
		}
	}

	/**
	 * 
	 */
	public void shutdown() {
		if (!isEnabled())
			return;
		
		// Stop the http client
		//httpClient.getConnectionManager().shutdown();
		
		// Stop the data executor
		jobQueue.clear();
		executor.shutdown();
		try {
			if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
				logger.error(name + "'s data pool did not terminate");
				executor.shutdownNow();
				if (!executor.awaitTermination(1, TimeUnit.SECONDS))
					logger.error(name + "'s data pool did not terminate again");
			}
		} catch (InterruptedException ie) {
			logger.error(name + " was interrupted");
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}

	}

	/**
	 * 
	 */
	public void start() {
		// Create a scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));

		// Create a connection manager
		connManager = new ThreadSafeClientConnManager(httpParams,
				schemeRegistry);

		// Create an HTTP client, disable cookies and don't retry requests
		httpClient = new DefaultHttpClient(connManager, httpParams);
		((DefaultHttpClient) httpClient).setCookieStore(null);
		((DefaultHttpClient) httpClient).setCookieSpecs(null);
		((DefaultHttpClient) httpClient).setHttpRequestRetryHandler(null);

		// Create an other executor for the data service
		//executor = Executors.newFixedThreadPool(5);
		jobQueue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(2, 2, 10, TimeUnit.SECONDS, jobQueue);
		((ThreadPoolExecutor) executor).prestartAllCoreThreads();

		setEnabled(true);
	}

	/**
	 * @param resources
	 */
	public void executeCacheUpdateTask(NodeSet resources) {
		executor.execute(new CacheUpdateTask(this, resources));
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return query URL
	 */
	public URI getURI() {
		return URI;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return URI.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EndPoint))
			return false;
		EndPoint other = (EndPoint) obj;
		return URI.equals(other.URI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return URI.toString();
	}

	/**
	 * @param requestsCounter
	 *            the requestsCounter to set
	 */
	public synchronized void setRequestsCounter(long requestsCounter) {
		this.requestsCounter = requestsCounter;
	}

	/**
	 * @return the requestsCounter
	 */
	public synchronized long getRequestsCounter() {
		return requestsCounter;
	}

	/**
	 * @param totalLatency
	 *            the totalLatency to set
	 */
	public synchronized void setTotalLatency(long totalLatency) {
		this.totalLatency = totalLatency;
	}

	/**
	 * @return the totalLatency
	 */
	public synchronized long getTotalLatency() {
		return totalLatency;
	}

	/**
	 * @param errorsCounter
	 *            the errorsCounter to set
	 */
	public synchronized void setErrorsCounter(long errorsCounter) {
		this.errorsCounter = errorsCounter;
	}

	/**
	 * @return the errorsCounter
	 */
	public synchronized long getErrorsCounter() {
		return errorsCounter;
	}

	/**
	 * @param informativeCounter
	 *            the informativeCounter to set
	 */
	public synchronized void setInformativeCounter(int informativeCounter) {
		this.informativeCounter = informativeCounter;
	}

	/**
	 * @return the informativeCounter
	 */
	public synchronized int getInformativeCounter() {
		return informativeCounter;
	}

	/**
	 * @return the job queue size
	 */
	public int getQueueSize() {
		return jobQueue.size();
	}

	/**
	 * @return the http client
	 */
	public HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * @return true if the end point seems to be usable
	 */
	public synchronized boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled
	 */
	private synchronized void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
