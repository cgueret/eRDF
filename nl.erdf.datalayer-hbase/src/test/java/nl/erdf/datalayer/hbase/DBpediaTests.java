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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class DBpediaTests {
	/** Logger */
	final Logger logger = LoggerFactory.getLogger(DBpediaTests.class);

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
		logger.info("Amsterdam is a " + dl.getResource(new Triple(s, p, null)));
		Assert.assertTrue(dl.isValid(new Triple(s, p, null)));
		Assert.assertTrue(dl.isValid(new Triple(s, null, o)));
		Assert.assertTrue(dl.isValid(new Triple(null, p, o)));

		p = f.createURI("http://dbpedia.org/ontology/birthName");
		o = f.createURI("http://dbpedia.org/resource/Kingdom_of_the_Netherlands");
		Assert.assertFalse(dl.isValid(new Triple(null, p, o)));

		s = f.createURI("http://dbpedia.org/resource/M._C._Escher");
		p = f.createURI("http://dbpedia.org/ontology/field");
		logger.info("Field of Escher is " + dl.getResource(new Triple(s, p, null)));
		Assert.assertTrue(dl.isValid(new Triple(s, p, null)));

		o = f.createURI("http://dbpedia.org/resource/Mariah_Carey");
		p = f.createURI("http://dbpedia.org/ontology/artist");
		logger.info("Album of Mariah_Carey " + dl.getResource(new Triple(null, p, o)));
		Assert.assertTrue(dl.isValid(new Triple(null, p, o)));

		s = f.createURI("http://dbpedia.org/resource/Hip_Hop_Is_Dead");
		p = f.createURI("http://dbpedia.org/ontology/artist");
		logger.info("Artist of Hip_Hop_Is_Dead is " + dl.getResource(new Triple(s, p, null)));
		Assert.assertTrue(dl.isValid(new Triple(s, p, null)));

	}
}
