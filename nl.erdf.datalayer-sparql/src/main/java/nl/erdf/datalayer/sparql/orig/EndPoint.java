/**
 * 
 */
package nl.erdf.datalayer.sparql.orig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nl.erdf.datalayer.sparql.LIFOQueue;
import nl.erdf.datalayer.sparql.RetryHandler;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tolgam
 * 
 */
public class EndPoint {
	// Logger instance
	static final Logger logger = LoggerFactory.getLogger(EndPoint.class);

	// The name of this end point
	private final String name;

	// The URI poiting to it
	private final URI URI;

	// Executor to run the update tasks against this end point
	private ExecutorService executor;
	private BlockingQueue<Runnable> jobQueue;

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
	public EndPoint(final String name, final String address) throws URISyntaxException {
		this.name = name;
		this.URI = new URI(address);

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
		setEnabled(false);

		// Cancel remaining tasks
		for (Runnable runnable : jobQueue)
			if (runnable instanceof CacheUpdateTask)
				((CacheUpdateTask) runnable).cancel();
		jobQueue.clear();

		// Wait for the running ones to be stopped
		try {
			while (((ThreadPoolExecutor) executor).getActiveCount() != 0)
				Thread.sleep(100);
		} catch (InterruptedException ie) {
		}

		// Stop the data executor
		executor.shutdown();
		try {
			if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
				executor.shutdownNow();
				if (!executor.awaitTermination(1, TimeUnit.SECONDS))
					logger.error(name + "'s data pool did not terminate");
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
	public void start(ClientConnectionManager connManager) {
		// Create an HTTP client, disable cookies and don't retry requests
		httpClient = new DefaultHttpClient(connManager);
		httpClient.setCookieStore(null);
		httpClient.setCookieSpecs(null);
		httpClient.setHttpRequestRetryHandler(new RetryHandler());
		
		// Set connectivity params
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 2000);
		HttpConnectionParams.setSoTimeout(params, 1000);
		HttpConnectionParams.setTcpNoDelay(params, true);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUseExpectContinue(params, true);

		// Create an other executor for the data service
		// executor = Executors.newFixedThreadPool(5);
		jobQueue = new LIFOQueue<Runnable>();
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
