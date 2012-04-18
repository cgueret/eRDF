/**
 * 
 */
package nl.erdf.datalayer.sparql;

import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nl.erdf.model.EndPoint;
import nl.erdf.util.LIFOQueue;
import nl.erdf.util.RetryHandler;

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
public class EndPointExecutor {
	// Logger instance
	static final Logger logger = LoggerFactory.getLogger(EndPointExecutor.class);

	// The end point
	private final EndPoint endPoint;

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

	/**
	 * @param name
	 * @param address
	 * @throws URISyntaxException
	 */
	public EndPointExecutor(EndPoint endPoint) {
		this.endPoint = endPoint;
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
					logger.error(endPoint.getURI() + "'s data pool did not terminate");
			}
		} catch (InterruptedException ie) {
			logger.error(endPoint.getURI() + " was interrupted");
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
	}

	/**
	 * @param resources
	 */
	public void executeCacheUpdateTask(NodeSet resources) {
		executor.execute(new CacheUpdateTask(this, resources));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return endPoint.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EndPointExecutor))
			return false;
		EndPointExecutor other = (EndPointExecutor) obj;
		return endPoint.equals(other.endPoint);
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
	 * @return
	 */
	public EndPoint getEndPoint() {
		return endPoint;
	}
}
