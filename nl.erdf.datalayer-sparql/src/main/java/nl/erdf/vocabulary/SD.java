/**
 * 
 */
package nl.erdf.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
public class SD {
	/** http://www.w3.org/ns/sparql-service-description# */
	public static final String NAMESPACE = "http://www.w3.org/ns/sparql-service-description#";

	/** sd:Service */
	public final static URI SERVICE;

	/** sd:defaultGraph */
	public final static URI DEFAULT_GRAPH;

	/** sd:endpoint */
	public final static URI ENDPOINT;

	/** sd:supportedLanguage */
	public final static URI SUPPORTED_LANGUAGE;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		SERVICE = factory.createURI(SD.NAMESPACE, "Service");
		DEFAULT_GRAPH = factory.createURI(SD.NAMESPACE, "defaultGraph");
		ENDPOINT = factory.createURI(SD.NAMESPACE, "endpoint");
		SUPPORTED_LANGUAGE = factory.createURI(SD.NAMESPACE, "supportedLanguage");
	}

}
