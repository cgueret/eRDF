/**
 * 
 */
package nl.erdf.datalayer.hbase;

import java.io.IOException;
import java.util.ArrayList;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Triple;
import nl.vu.datalayer.hbase.schema.HBHexastoreSchema;

import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.rest.client.Client;
import org.apache.hadoop.hbase.rest.client.Cluster;
import org.apache.hadoop.hbase.rest.client.RemoteHTable;
import org.apache.hadoop.hbase.util.Bytes;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class RestHBaseDataLayer implements DataLayer {
	private final Client client;
	private final ArrayList<ArrayList<Integer>> keyPositions = new ArrayList<ArrayList<Integer>>();
	private final ArrayList<ArrayList<Integer>> valuePositions = new ArrayList<ArrayList<Integer>>();
	private static Logger logger = LoggerFactory.getLogger("HexastoreLogger");

	/**
	 * 
	 */
	public RestHBaseDataLayer(String name, int port) {
		// Initiate the connection with HBase
		Cluster cluster = new Cluster();
		cluster.add(name, port);
		client = new Client(cluster);

		// Compute some stuff
		for (int i = 0; i < HBHexastoreSchema.TABLE_COUNT; i++) {
			int bitCount = Integer.bitCount(i + 1);

			ArrayList<Integer> keyPosition = new ArrayList<Integer>(bitCount);
			ArrayList<Integer> valuePosition = new ArrayList<Integer>(3 - bitCount);
			computeKeyAndValuePositions(i + 1, keyPosition, valuePosition);

			logger.info("Table: " + HBHexastoreSchema.TABLE_NAMES[i] + " Index: " + Integer.toString(i + 1)
					+ "; BitCount: " + Integer.toString(bitCount));
			logger.info("KeyPositions: " + keyPosition);

			// logger.info("ValuePositions: ");
			// for (int j = 0; j < valuePosition.size(); j++) {
			// logger.info("valPosition: " + valuePosition.get(j));
			// }

			keyPositions.add(keyPosition);
			valuePositions.add(valuePosition);
		}
	}

	private void computeKeyAndValuePositions(int index, ArrayList<Integer> keyPosition, ArrayList<Integer> valuePosition) {
		// the elements from the triple S-P-O that make up the key correspond to
		// the 1 bits in the index parameter
		// e.g. if index = 5 (binary 101) then the key is SO and the value is P
		// if index = 4 (binary 100) then the key is S and the values is PO

		int position = 0;
		for (int i = 0; i < 3; i++) {// assuming the index is at most 7
			if ((index & 1) == 1) {
				keyPosition.add(position);
			} else {
				valuePosition.add(position);
			}
			index >>= 1;
			position++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.erdf.datalayer.DataLayer#getNumberOfResources(nl.erdf.model.Triple)
	 */
	public long getNumberOfResources(Triple pattern) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#getResource(nl.erdf.model.Triple)
	 */
	public Value getResource(Triple pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#isValid(nl.erdf.model.Triple)
	 */
	public boolean isValid(Triple pattern) {
		try {
			System.out.println(getRawCellValue(new Triple(new URIImpl(
					"http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseases/21"), new URIImpl(
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), null)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param triple
	 * @return
	 * @throws IOException
	 */
	public String getRawCellValue(Triple triple) throws IOException {
		byte[] key = getKey(triple);
		System.out.println(Bytes.toStringBinary(key));
		Get get = new Get(key);
		RemoteHTable table = new RemoteHTable(client, getTable(triple));
		Result r = table.get(get);
		System.out.println(r);
		// byte[] value = r.getValue(HBHexastoreSchema.COLUMN_FAMILY.getBytes(),
		// HBHexastoreSchema.COLUMN_NAME.getBytes());
		// if (value != null)
		// return Bytes.toString(value);
		// else
		return null;

	}

	/**
	 * @param triple
	 * @return
	 */
	private String getTable(Triple triple) {
		int index = 0;
		if (triple.getSubject() != null)
			index += 4;
		if (triple.getPredicate() != null)
			index += 2;
		if (triple.getObject() != null)
			index += 1;

		return HBHexastoreSchema.TABLE_NAMES[index - 1];
	}

	/**
	 * @param triple
	 * @param index
	 * @return
	 */
	private byte[] getKey(Triple triple) {
		// Inverse the triple
		String[] inversedTriple = new String[3];
		inversedTriple[0] = (triple.getObject() == null ? null : triple.getObject().stringValue());
		inversedTriple[1] = (triple.getPredicate() == null ? null : triple.getPredicate().stringValue());
		inversedTriple[2] = (triple.getSubject() == null ? null : triple.getSubject().stringValue());

		int index = 0;
		for (int i = 0; i < inversedTriple.length; i++) {
			if (inversedTriple[i] != null) {
				index |= 1 << i;
			}
		}

		System.out.println(index);
		ArrayList<Integer> keyMask = keyPositions.get(index - 1);
		System.out.println(keyMask);
		// construct the by concatenating the hashes of each element
		long[] hashes = new long[keyMask.size()];
		for (int i = 0; i < hashes.length; i++) {
			hashes[i] = hashFunction(inversedTriple[keyMask.get(i)]);
			System.out.println("Hash " + inversedTriple[keyMask.get(i)] + " = " + hashes[i]);
		}

		byte[] key;
		switch (hashes.length) {
		case 3:
			key = Bytes.add(Bytes.toBytes(hashes[2]), Bytes.toBytes(hashes[1]), Bytes.toBytes(hashes[0]));
			break;
		case 2:
			key = Bytes.add(Bytes.toBytes(hashes[1]), Bytes.toBytes(hashes[0]));
			break;
		case 1:
			key = Bytes.toBytes(hashes[0]);
			break;
		default:
			throw new RuntimeException("Unexpected number of bits");
		}

		return key;
	}

	/**
	 * Hash function to convert any string to an 8-byte integer
	 * 
	 * @param key
	 * @return
	 */
	private long hashFunction(String key) {
		long hash = 0;

		for (int i = 0; i < key.length(); i++) {
			hash = key.charAt(i) + (hash << 6) + (hash << 16) - hash;
		}

		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#add(nl.erdf.model.Triple)
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
		client.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.erdf.datalayer.DataLayer#waitForLatencyBuffer()
	 */
	public void waitForLatencyBuffer() {
		// TODO Auto-generated method stub

	}

}
