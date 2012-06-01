/**
 * 
 */
package nl.erdf.datalayer.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Triple;
import nl.vu.datalayer.hbase.HBaseClientSolution;
import nl.vu.datalayer.hbase.HBaseFactory;
import nl.vu.datalayer.hbase.connection.HBaseConnection;
import nl.vu.datalayer.hbase.schema.HBPrefixMatchSchema;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class RestHBaseDataLayer implements DataLayer {
	
	private HBaseConnection con;
	
	private HBaseClientSolution sol;
	
	private Random r;
	
	private ArrayList<ArrayList<Value>> results = null;

	private int unboundPosition = -1;
	
	public RestHBaseDataLayer() {
		try {
			con = HBaseConnection.create(HBaseConnection.REST);
			sol = HBaseFactory.getHBaseSolution(HBPrefixMatchSchema.SCHEMA_NAME, con, null);
			
			r = new Random();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int retrieveInternal(Triple pattern) throws IOException{	
		Value[] quad = { pattern.getSubject(), pattern.getPredicate(), pattern.getObject(), pattern.getContext() };
		int unboundPosition = -1;
		for (int i = 0; i < quad.length; i++) {
			if (quad[i] == null) {
				unboundPosition = i;
				break;
			}
		}
		results = sol.util.getResults(quad);

		return unboundPosition;
	}
	
	
	public long getNumberOfResources(Triple pattern) {
		try {
			if (results != null){
				return results.size();
			}
			
			unboundPosition = retrieveInternal(pattern);	
			return results.size();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}	
	}

	public Value getResource(Triple pattern) {
		try {
			if (results == null){
				unboundPosition = retrieveInternal(pattern);
			}
			
			int resultIndex = r.nextInt(results.size());
			return results.get(resultIndex).get(unboundPosition);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean isValid(Triple pattern) {
		if (pattern == null)
			return false;
		
		return (pattern.getNumberNulls() == 1);
	}

	public void add(Statement statement) {
		// TODO Auto-generated method stub
		
	}

	public void clear() {
		results = null;
		unboundPosition = -1;
	}

	public void shutdown() {
		try {
			con.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void waitForLatencyBuffer() {
		// TODO Auto-generated method stub
		
	}

}
