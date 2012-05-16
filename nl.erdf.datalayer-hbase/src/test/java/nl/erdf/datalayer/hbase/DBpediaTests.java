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
		System.out.println("Amsterdam is a " + dl.getResource(new Triple(s, p, null)));
		Assert.assertTrue(dl.isValid(new Triple(s, p, null)));
		Assert.assertTrue(dl.isValid(new Triple(s, null, o)));
		Assert.assertTrue(dl.isValid(new Triple(null, p, o)));

		URI p2 = f.createURI("http://dbpedia.org/ontology/birthName");
		Value o2 = f.createURI("http://dbpedia.org/resource/Kingdom_of_the_Netherlands");
		Assert.assertFalse(dl.isValid(new Triple(null, p2, o2)));

		Resource s2 = f.createURI("http://dbpedia.org/resource/Hip_Hop_Is_Dead");
		p2 = f.createURI("http://dbpedia.org/ontology/artist");
		System.out.println("Artist of album is " + dl.getResource(new Triple(s2, p2, null)));

		Resource s3 = f.createURI("http://dbpedia.org/resource/M._C._Escher");
		p2 = f.createURI("http://dbpedia.org/ontology/field");
		System.out.println("Field of Escher is " + dl.getResource(new Triple(s3, p2, null)));

	}
}
