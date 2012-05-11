/**
 * 
 */
package nl.erdf.datalayer.hbase;

import java.io.IOException;

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
import org.openrdf.model.vocabulary.RDF;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class DBpediaTests {
	private NativeHBaseDataLayer dl = null;

	/**
	 * @throws IOException
	 */
	@Test
	@Before
	public void setUp() throws IOException {
		dl = new NativeHBaseDataLayer();
		Assert.assertTrue(dl != null);
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
	public void testAmsterdam() {
		ValueFactory f = new ValueFactoryImpl();

		Resource s = f.createURI("http://dbpedia.org/resource/Amsterdam");
		URI p = RDF.TYPE;
		Value o = f.createURI("http://dbpedia.org/ontology/PopulatedPlace");
		Assert.assertTrue(dl.isValid(new Triple(s, p, o)));
		System.out.println(dl.getResource(new Triple(s, p, null)));
		Assert.assertTrue(dl.isValid(new Triple(s, p, null)));
		Assert.assertTrue(dl.isValid(new Triple(s, null, o)));
		Assert.assertTrue(dl.isValid(new Triple(null, p, o)));
	}
}
