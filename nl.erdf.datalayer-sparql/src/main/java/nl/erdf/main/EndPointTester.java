/**
 * 
 */
package nl.erdf.main;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;


/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class EndPointTester extends Thread {
	private final static Query query = QueryFactory.create("SELECT * WHERE {?s ?p ?o} LIMIT 1");
	private final QueryExecution qexec;

	private boolean intime = true;

	private boolean valid = false;

	private String error = "no information";

	/**
	 * @param URI
	 */
	public EndPointTester(String URI) {
		super(URI);
		this.qexec = QueryExecutionFactory.sparqlService(URI, query);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public void run() {
		try {
			ResultSet results = qexec.execSelect();
			if (inTime() && results.hasNext()) {
				setValid(true);
			} else {
				error = "no result";
			}
		} catch (Exception e) {
			error = e.getMessage();
		} finally {
			qexec.close();
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
		qexec.abort();
		error = "time out";
	}

	/**
	 * @return the error message, if there was one
	 */
	public synchronized String getError() {
		return error;
	}
}
