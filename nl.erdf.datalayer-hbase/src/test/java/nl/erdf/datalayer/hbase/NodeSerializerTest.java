/**
 * 
 */
package nl.erdf.datalayer.hbase;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class NodeSerializerTest {
	protected ValueFactory f = new ValueFactoryImpl();

	/**
	 * Test URIs
	 */
	@Test
	public void testURI() {
		URI uri = f.createURI("http://dbpedia.org/resource/Amsterdam");
		byte[] bytes = NodeSerializer.toBytes(uri);
		URI uriB = (URI) NodeSerializer.fromBytes(bytes);
		Assert.assertTrue(uri.equals(uriB));
	}

	/**
	 * Test literals
	 */
	@Test
	public void testLiterals() {
		// Plain
		Literal plain = f.createLiteral("ceci est un test");
		byte[] bytesPlain = NodeSerializer.toBytes(plain);
		Literal plainB = (Literal) NodeSerializer.fromBytes(bytesPlain);
		Assert.assertTrue(plain.equals(plainB));

		// Localised
		Literal local = f.createLiteral("ceci est un test", "fr");
		byte[] bytesLocal = NodeSerializer.toBytes(local);
		Literal localB = (Literal) NodeSerializer.fromBytes(bytesLocal);
		Assert.assertTrue(local.equals(localB));

		// Typed
		Literal ten = f.createLiteral(10);
		byte[] bytesTen = NodeSerializer.toBytes(ten);
		Literal tenB = (Literal) NodeSerializer.fromBytes(bytesTen);
		Assert.assertTrue(ten.equals(tenB));
	}

	/**
	 * Test BNodes
	 */
	@Test
	public void testBNode() {
		BNode node = f.createBNode("identifier");
		byte[] bytesNode = NodeSerializer.toBytes(node);
		BNode nodeB = (BNode) NodeSerializer.fromBytes(bytesNode);
		Assert.assertTrue(node.equals(nodeB));
	}
}
