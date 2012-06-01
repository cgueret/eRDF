package nl.erdf.datalayer.hbase;

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
import org.openrdf.rio.ntriples.NTriplesUtil;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class RestHBaseDataLayerTest {
	private RestHBaseDataLayer dl = null;
	
	private ValueFactory valueFactory = null; 

	/**
	 * 
	 */
	@Test
	@Before
	public void setUp() {
		valueFactory = new ValueFactoryImpl();
		dl = new RestHBaseDataLayer();
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
	public void testValid() {
		System.out.println(dl.isValid(null));
	}
	
	@Test
	public void testReadWrite() {
		
		Resource subject = NTriplesUtil.parseResource("<http://dbpedia.org/resource/Alabama>", valueFactory);
		URI predicate =  NTriplesUtil.parseURI("<http://dbpedia.org/ontology/PopulatedPlace/areaTotal>", valueFactory);
		Resource context = NTriplesUtil.parseResource("<http://en.wikipedia.org/wiki/Alabama#absolute-line=33>", valueFactory);
		Triple t = new Triple(subject, predicate, null, context);
		
		Value result = dl.getResource(t);
		
		System.out.println(result);
		dl.clear();
	}
	
	@Test
	public void testReadWrite2() {
		
		Resource subject = NTriplesUtil.parseResource("<http://dbpedia.org/resource/Wim_Sonneveld>", valueFactory);
		URI predicate =  NTriplesUtil.parseURI("<http://dbpedia.org/ontology/wikiPageWikiLink>", valueFactory);
		Resource context = NTriplesUtil.parseResource("<http://en.wikipedia.org/wiki/Wim_Sonneveld#absolute-line=29>", valueFactory);
		Triple t = new Triple(subject, predicate, null, context);
		
		Value result = dl.getResource(t);
		
		System.out.println(result);
		dl.clear();
	}
	
	@Test
	public void testNumberOfResources() {
		
		Resource subject = NTriplesUtil.parseResource("<http://dbpedia.org/resource/Wim_Sonneveld>", valueFactory);
		URI predicate =  NTriplesUtil.parseURI("<http://dbpedia.org/ontology/wikiPageWikiLink>", valueFactory);
		Resource context = NTriplesUtil.parseResource("<http://en.wikipedia.org/wiki/Wim_Sonneveld#absolute-line=29>", valueFactory);
		Triple t = new Triple(subject, predicate, null, context);
		
		long resultsNo = dl.getNumberOfResources(t);
		
		System.out.println(resultsNo);
	}

}
