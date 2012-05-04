package nl.erdf.datalayer.hbase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class RestHBaseDataLayerTest {
	private RestHBaseDataLayer dl = null;

	/**
	 * 
	 */
	@Test
	@Before
	public void setUp() {
		dl = new RestHBaseDataLayer("fs0.das4.cs.vu.nl", 8090);
		Assert.assertTrue(dl != null);
		dl.clear();
	}

	/**
	 * 
	 */
	@Test
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
		System.out.println(dl.isValid(null));
	}

}
