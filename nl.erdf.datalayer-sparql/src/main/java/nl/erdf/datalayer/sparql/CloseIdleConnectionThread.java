package nl.erdf.datalayer.sparql;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
// Code copied from LDSpider http://code.google.com/p/ldspider/ and adapted
public class CloseIdleConnectionThread extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(CloseIdleConnectionThread.class);

	private ClientConnectionManager cm;
	private long sleepTime;
	private boolean alive;

	/**
	 * @param cm
	 * @param sleepTime
	 */
	public CloseIdleConnectionThread(ClientConnectionManager cm, long sleepTime) {
		this.cm = cm;
		this.sleepTime = sleepTime;
		logger.info("Initialised with sleepTime " + sleepTime + " ms");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		logger.info("Starting");
		alive = true;

		while (alive) {
			logger.info("Closing expired and idle connections");
			cm.closeExpiredConnections();
			cm.closeIdleConnections(0L, TimeUnit.SECONDS);

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		logger.info("Stopped");
	}

	/**
	 * 
	 */
	public void shutdown() {
		alive = false;
		logger.info("Stopping");
		interrupt();
	}
}
