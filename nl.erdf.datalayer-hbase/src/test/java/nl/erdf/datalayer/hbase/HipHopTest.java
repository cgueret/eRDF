/**
 * 
 */
package nl.erdf.datalayer.hbase;

import java.io.IOException;

import nl.erdf.datalayer.DataLayer;
import nl.erdf.main.DataLoader;
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
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class HipHopTest {
	/** Logger */
	final Logger logger = LoggerFactory.getLogger(HipHopTest.class);

	// Datalayer
	private DataLayer dl = null;

	/**
	 * @throws IOException
	 * @throws RDFHandlerException
	 * @throws RDFParseException
	 */
	@Before
	public void setUp() throws IOException, RDFParseException, RDFHandlerException {
		// Connect to the data layer
		dl = NativeHBaseDataLayer.getInstance("test");

		// Clear the previous content
		dl.clear();

		// Load the dataset
		logger.info("Start load data");
		DataLoader dataLoader = new DataLoader(dl);
		dataLoader.load("data/hip_hop.nt.bz2");
		logger.info("End load data");

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
	public void testAlbumArtist() {
		ValueFactory f = new ValueFactoryImpl();

		// <http://dbpedia.org/resource/Hip_Hop_Is_Dead>
		// <http://dbpedia.org/ontology/artist>
		// <http://dbpedia.org/resource/Nas> .
		Resource s = f.createURI("http://dbpedia.org/resource/Hip_Hop_Is_Dead");
		URI p = f.createURI("http://dbpedia.org/ontology/artist");
		URI p2 = f.createURI("http://dbpedia.org/ontology/recordLabel");
		Value o = f.createURI("http://dbpedia.org/resource/Nas");

		logger.info("Testing \"isValid\" combinations");
		for (int i = 0; i < 100; i++) {
			Assert.assertTrue(dl.isValid(new Triple(s, p, o)));
			Assert.assertTrue(dl.isValid(new Triple(s, p, null)));
			Assert.assertTrue(dl.isValid(new Triple(s, null, o)));
			Assert.assertTrue(dl.isValid(new Triple(null, p, o)));
		}

		logger.info("Testing \"getResource\" combinations");
		for (int i = 0; i < 100; i++) {
			Assert.assertTrue(dl.getResource(new Triple(s, p, null)).equals(o));
			Value pred = dl.getResource(new Triple(s, null, o));
			Assert.assertTrue(pred.equals(p) || pred.equals(p2));
			Assert.assertTrue(dl.getResource(new Triple(null, p, o)).equals(s));
		}

		/*
		 * p = f.createURI("http://dbpedia.org/ontology/birthName"); o =
		 * f.createURI
		 * ("http://dbpedia.org/resource/Kingdom_of_the_Netherlands");
		 * Assert.assertFalse(dl.isValid(new Triple(null, p, o)));
		 * 
		 * s = f.createURI("http://dbpedia.org/resource/M._C._Escher"); p =
		 * f.createURI("http://dbpedia.org/ontology/field");
		 * logger.info("Field of Escher is " + dl.getResource(new Triple(s, p,
		 * null))); Assert.assertTrue(dl.isValid(new Triple(s, p, null)));
		 * 
		 * o = f.createURI("http://dbpedia.org/resource/Mariah_Carey"); p =
		 * f.createURI("http://dbpedia.org/ontology/artist");
		 * logger.info("Album of Mariah_Carey " + dl.getResource(new
		 * Triple(null, p, o))); Assert.assertTrue(dl.isValid(new Triple(null,
		 * p, o)));
		 * 
		 * s = f.createURI("http://dbpedia.org/resource/Hip_Hop_Is_Dead"); p =
		 * f.createURI("http://dbpedia.org/ontology/artist");
		 * logger.info("Artist of Hip_Hop_Is_Dead is " + dl.getResource(new
		 * Triple(s, p, null))); Assert.assertTrue(dl.isValid(new Triple(s, p,
		 * null)));
		 */

	}
}
