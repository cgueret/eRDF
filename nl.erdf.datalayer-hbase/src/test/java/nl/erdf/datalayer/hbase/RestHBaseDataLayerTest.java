package nl.erdf.datalayer.hbase;

import nl.erdf.model.Triple;
import nl.vu.datalayer.hbase.connection.HBaseConnection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class RestHBaseDataLayerTest {
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(RestHBaseDataLayerTest.class);

	private SeverHBaseDataLayer dl = null;

	private ValueFactory valueFactory = null;

	/**
	 * 
	 */
	@Test
	@Before
	public void setUp() {
		valueFactory = new ValueFactoryImpl();
		dl = new SeverHBaseDataLayer(HBaseConnection.REST, true);
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

	/*
	 * @Test public void testArea() { Resource s =
	 * NTriplesUtil.parseResource("<http://dbpedia.org/resource/Alabama>",
	 * valueFactory); URI p =
	 * valueFactory.createURI("http://dbpedia.org/ontology/PopulatedPlace/areaTotal"
	 * ); Literal o = valueFactory.createLiteral("135765.",
	 * valueFactory.createURI("http://dbpedia.org/datatype/squareKilometre"));
	 * Resource c = NTriplesUtil
	 * .parseResource("<http://en.wikipedia.org/wiki/Alabama#absolute-line=33>",
	 * valueFactory); Assert.assertTrue(dl.getResource(new Triple(s, null, o,
	 * c)).equals(p)); }
	 */

	@Test
	public void testDBpediaArtist() {
		URI p = valueFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
		URI o = valueFactory.createURI("http://dbpedia.org/ontology/MartialArtist");
		Assert.assertTrue(dl.getNumberOfResources(new Triple(null, p, o)) > 0);
	}

	@Test
	public void testDBpediaArtist2() {
		URI p = valueFactory.createURI("http://dbpedia.org/ontology/artist");
		URI o = valueFactory.createURI("http://dbpedia.org/resource/Jimmy_Sturr");
		logger.info(dl.getResource(new Triple(null, p, o)).toString());
	}

	@Test
	public void testFreeBaseArtist() {
		URI s = valueFactory.createURI("http://rdf.freebase.com/ns/m.0100346");
		URI p = valueFactory.createURI("http://rdf.freebase.com/ns/music.track.artist");
		Value o = valueFactory.createURI("http://rdf.freebase.com/ns/m.01qmmjp");
		Assert.assertTrue(dl.getResource(new Triple(s, p, null)).equals(o));
		Assert.assertTrue(dl.getResource(new Triple(s, null, o)).equals(p));
		Assert.assertTrue(dl.getNumberOfResources(new Triple(null, p, o)) > 0);
		for (int i = 0; i < 10; i++)
			logger.info(dl.getResource(new Triple(null, p, o)) + "");
	}
}
