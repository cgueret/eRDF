package nl.erdf.datalayer.hbase;

import java.io.IOException;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;

import nl.erdf.datalayer.QueryPattern;

/**
 * Test. Takes as single argument the number of triples to check. IT WILL DELETE
 * EVERYTHING IN HBASE. It should fail 50% of the time ;-)
 */
public class HBaseDataLayerTest {
	private HBaseDataLayer dl = null;
	private int count = 1;

	/**
	 * @throws IOException
	 */
	@Test
	@Before
	public void setUp() throws IOException {
		dl = new HBaseDataLayer();
		Assert.assertTrue(dl != null);
		dl.clear();
	}

	/**
	 * 
	 */
	@After
	public void tearDown() {
		if (dl != null)
			dl.shutdown();
		dl = null;
	}

	/**
	 * 
	 */
	@Test
	public void testReadWrite() {

		Random r1 = new Random(0);

		// Write values
		for (int i = 0; i < count; i++) {
			Node s = Node.createURI("http://" + r1.nextLong());
			Node p = Node
					.createAnon(AnonId.create(Long.toString(r1.nextLong())));
			Node o = Node.createLiteral(r1.nextLong() + "", "no", null);
			Node o2 = Node.createLiteral(r1.nextLong() + "", "no", null);

			dl.insert(s, p, o);
			dl.insert(s, p, o2);
		}

		Random r2 = new Random();
		r1 = new Random(0); // Use the same seed to get the same values

		// Read values
		for (int i = 0; i < count; i++) {
			Node s = Node.createURI("http://" + r1.nextLong());
			Node p = Node
					.createAnon(AnonId.create(Long.toString(r1.nextLong())));
			Node o = Node.createLiteral(r1.nextLong() + "", "no", null);

			Assert.assertTrue(dl.getRandomResource(r2,
					new QueryPattern(s, p, QueryPattern.WILDCARD)).equals(o));

			Assert.assertTrue(dl.getRandomResource(r2,
					new QueryPattern(QueryPattern.WILDCARD, p, o)).equals(s));

			Assert.assertTrue(dl.getRandomResource(r2,
					new QueryPattern(s, QueryPattern.WILDCARD, o)).equals(p));
		}

	}
}
