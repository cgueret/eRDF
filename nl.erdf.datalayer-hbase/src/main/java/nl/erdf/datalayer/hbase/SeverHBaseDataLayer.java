/**
 * 
 */
package nl.erdf.datalayer.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Triple;
import nl.erdf.util.Randomizer;
import nl.vu.datalayer.hbase.HBaseClientSolution;
import nl.vu.datalayer.hbase.HBaseFactory;
import nl.vu.datalayer.hbase.connection.HBaseConnection;
import nl.vu.datalayer.hbase.schema.HBPrefixMatchSchema;

import org.apache.jcs.JCS;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SeverHBaseDataLayer implements DataLayer {
	// Logger
	protected static final Logger logger = LoggerFactory.getLogger(SeverHBaseDataLayer.class);

	private HBaseConnection con;

	private HBaseClientSolution sol;

	// Cache for the number of resources
	private final Map<Triple, Long> countersCache = new HashMap<Triple, Long>();

	// General cache
	private JCS resultCache;

	/**
	 * @param connectionType
	 * @param useCache
	 * 
	 */
	public SeverHBaseDataLayer(byte connectionType) {
		try {
			con = HBaseConnection.create(connectionType);
			sol = HBaseFactory.getHBaseSolution(HBPrefixMatchSchema.SCHEMA_NAME, con, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			resultCache = JCS.getInstance("validate");
		} catch (org.apache.jcs.access.exception.CacheException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.datalayer.DataLayer#getNumberOfResources(nl.erdf.model.Triple)
	 */
	public synchronized long getNumberOfResources(Triple pattern) {
		long results = 1000;

		if (countersCache.containsKey(pattern)) {
			results = countersCache.get(pattern);
		} else {
			logger.info("[CNT] " + pattern);
			Value[] quad = { pattern.getSubject(), pattern.getPredicate(), pattern.getObject(), pattern.getContext() };
			try {
				results = sol.util.countResults(quad, 1000);
			} catch (Exception e) {
				logger.error("Error in datalayer !");
				e.printStackTrace();
			}
			countersCache.put(pattern, new Long(results));
		}

		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#getResource(nl.erdf.model.Triple)
	 */
	public Value getResource(Triple pattern) {
		logger.info("[GET] " + pattern);

		// Get all the triples that match the pattern
		Value[] quad = { pattern.getSubject(), pattern.getPredicate(), pattern.getObject(), null };
		ArrayList<Value> t = null;
		try {
			t = sol.util.getSingleResult(quad, Randomizer.instance());
		} catch (Exception e) {
			logger.error("Error in datalayer !");
			e.printStackTrace();
		}

		// In case there is no result, return null
		if (t == null)
			return null;

		// Return the value for the sought variable (assuming there is only
		// one)
		if (pattern.getSubject() == null)
			return t.get(0);
		else if (pattern.getPredicate() == null)
			return t.get(1);
		else if (pattern.getObject() == null)
			return t.get(2);
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#isValid(nl.erdf.model.Triple)
	 */
	public boolean isValid(Triple pattern) {
		// null patterns can not be valid
		if (pattern == null)
			return false;

		Boolean result = (Boolean) resultCache.get(pattern);
		if (result == null) {
			result = new Boolean(false);
			if (pattern.getNumberNulls() != 0) {
				// Check a partial pattern
				Value[] quad = { pattern.getSubject(), pattern.getPredicate(), pattern.getObject(), null };
				boolean b = false;
				try {
					b = sol.util.hasResults(quad);
				} catch (Exception e) {
					logger.error("[VLD] " + pattern);
					e.printStackTrace();
				}
				result = new Boolean(b);
			} else {
				// Check a fully instantiated triple
				Value[] quad = { pattern.getSubject(), pattern.getPredicate(), null, null };
				ArrayList<ArrayList<Value>> results = new ArrayList<ArrayList<Value>>();
				try {
					results = sol.util.getResults(quad);
				} catch (Exception e) {
					logger.error("[VLD] " + pattern);
					e.printStackTrace();
				}

				if (results == null || results.size() == 0) {
					result = new Boolean(false);
				} else {
					for (ArrayList<Value> r : results)
						if (r.get(2) != null && r.get(2).equals(pattern.getObject()))
							result = new Boolean(true);
				}
			}
		}

		try {
			resultCache.put(pattern, result);
		} catch (org.apache.jcs.access.exception.CacheException e) {
			e.printStackTrace();
		}

		return result.booleanValue();

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
