package nl.erdf.datalayer.hbase;

import java.io.IOException;
import java.util.Random;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.model.Triple;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Test. Takes as single argument the number of triples to check. IT WILL DELETE
 * EVERYTHING IN HBASE. It should fail 50% of the time ;-)
 */
public class NativeHBaseDataLayerTest {
	private DataLayer dl = null;
	private int count = 10;

	/**
	 * @throws IOException
	 */
	@Test
	@Before
	public void setUp() throws IOException {
		// Connect to the data layer
		dl = NativeHBaseDataLayer.getInstance("test");
		Assert.assertTrue(dl != null);

		// Clear the previous content
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
		ValueFactory f = new ValueFactoryImpl();

		Random r1 = new Random(0);

		// Write values
		for (int i = 0; i < count; i++) {
			Resource s = f.createURI("http://" + r1.nextLong());
			URI p = f.createURI("http://" + r1.nextLong());
			Value o = f.createLiteral(r1.nextLong() + "", "no");
			dl.add(new Triple(s, p, o));
		}

		r1 = new Random(0); // Use the same seed to get the same values

		// Read values
		for (int i = 0; i < count; i++) {
			Resource s = f.createURI("http://" + r1.nextLong());
			URI p = f.createURI("http://" + r1.nextLong());
			Value o = f.createLiteral(r1.nextLong() + "", "no");

			Assert.assertTrue(dl.getResource(new Triple(s, p, null)).equals(o));
			Assert.assertTrue(dl.getResource(new Triple(null, p, o)).equals(s));
			Assert.assertTrue(dl.getResource(new Triple(s, null, o)).equals(p));
		}

	}
}
