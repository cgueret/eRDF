/**
 * 
 */
package nl.erdf.main;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class EndPointTester extends Thread {
	private final SPARQLRepository repository;
	private boolean intime = true;
	private boolean valid = false;
	private String error = "no information";

	/**
	 * @param URI
	 */
	public EndPointTester(String URI) {
		super(URI);
		repository = new SPARQLRepository(URI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public void run() {
		RepositoryConnection c = null;
		try {
			c = repository.getConnection();
			if (c.isOpen() && inTime() && !c.isEmpty()) {
				setValid(true);
			} else {
				error = "no result";
			}
		} catch (Exception e) {
			error = e.getMessage();
		} finally {
			try {
				if (c != null)
					c.close();
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return
	 */
	private synchronized boolean inTime() {
		return intime;
	}

	/**
	 * @return true if the end point is usable
	 */
	public synchronized boolean isValid() {
		return valid;
	}

	/**
	 * @param v
	 */
	private synchronized void setValid(boolean v) {
		this.valid = v;
	}

	/**
	 *
	 */
	public synchronized void stopQuery() {
		intime = false;
		error = "time out";
	}

	/**
	 * @return the error message, if there was one
	 */
	public synchronized String getError() {
		return error;
	}
}
