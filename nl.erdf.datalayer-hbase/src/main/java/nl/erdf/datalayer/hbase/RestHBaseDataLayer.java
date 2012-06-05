/**
 * 
 */
package nl.erdf.datalayer.hbase;

import java.io.IOException;
import java.util.ArrayList;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Triple;
import nl.erdf.util.Randomizer;
import nl.vu.datalayer.hbase.HBaseClientSolution;
import nl.vu.datalayer.hbase.HBaseFactory;
import nl.vu.datalayer.hbase.connection.HBaseConnection;
import nl.vu.datalayer.hbase.schema.HBPrefixMatchSchema;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class RestHBaseDataLayer implements DataLayer {
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(RestHBaseDataLayer.class);

	private HBaseConnection con;

	private HBaseClientSolution sol;

	/**
	 * 
	 */
	public RestHBaseDataLayer() {
		try {
			logger.info("Connecting to data layer");
			con = HBaseConnection.create(HBaseConnection.REST);
			sol = HBaseFactory.getHBaseSolution(HBPrefixMatchSchema.SCHEMA_NAME, con, null);
			logger.info("Ready!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param pattern
	 * @return
	 * @throws IOException
	 */
	private int retrieveInternal(Triple pattern) {
		if (pattern.getSubject() == null)
			return 0;
		else if (pattern.getPredicate() == null)
			return 1;
		else if (pattern.getObject() == null)
			return 2;
		else if (pattern.getContext() == null)
			return 3;
		else
			return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.datalayer.DataLayer#getNumberOfResources(nl.erdf.model.Triple)
	 */
	public long getNumberOfResources(Triple pattern) {
		logger.info("[CNT] " + pattern);
		try {
			Value[] quad = { pattern.getSubject(), pattern.getPredicate(), pattern.getObject(), pattern.getContext() };
			ArrayList<ArrayList<Value>> results = sol.util.getResults(quad);
			logger.info("[CNT] " + results.size());

			return results.size();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#getResource(nl.erdf.model.Triple)
	 */
	public Value getResource(Triple pattern) {
		logger.info("[GET] " + pattern);
		try {
			Value[] quad = { pattern.getSubject(), pattern.getPredicate(), pattern.getObject(), pattern.getContext() };
			ArrayList<ArrayList<Value>> results = sol.util.getResults(quad);

			Value res = null;
			if (results.size() != 0) {
				int resultIndex = Randomizer.instance().nextInt(results.size());
				res = results.get(resultIndex).get(retrieveInternal(pattern));
			}

			logger.info("[GET] " + res);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#isValid(nl.erdf.model.Triple)
	 */
	public boolean isValid(Triple pattern) {
		logger.info("[VLD] " + pattern);

		// null patterns can not be valid
		if (pattern == null)
			return false;

		if (pattern.getNumberNulls() != 0) {
			// Check a partial pattern
			return (getResource(pattern) != null);
		} else {
			// Check a fully instantiated triple
			try {
				Value[] quad = { pattern.getSubject(), pattern.getPredicate(), null, pattern.getContext() };
				ArrayList<ArrayList<Value>> results = sol.util.getResults(quad);
				for (ArrayList<Value> v : results)
					if (v.get(2).equals(pattern.getObject()))
						return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#add(org.openrdf.model.Statement)
	 */
	public void add(Statement statement) {
		// Not implemented
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#clear()
	 */
	public void clear() {
		// Not implemented
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#shutdown()
	 */
	public void shutdown() {
		try {
			con.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#waitForLatencyBuffer()
	 */
	public void waitForLatencyBuffer() {
		// Not implemented
	}

}
