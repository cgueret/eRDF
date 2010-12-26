/**
 * 
 */
package nl.erdf.datalayer.sparql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.openjena.atlas.json.JSON;
import org.openjena.atlas.json.JsonObject;
import org.openjena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tolgam
 * 
 */
// Use http://download.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html
public class Directory {
	/** Logger instance */
	private static final Logger logger = LoggerFactory.getLogger(Directory.class);

	/** A list of SPARQL end points */
	private LinkedList<EndPoint> listOfEndPoints = new LinkedList<EndPoint>();

	/**
	 * @return a copy of endPoints in that directory
	 */
	public Collection<EndPoint> endPoints() {
		return listOfEndPoints;
	}

	/**
	 * @param name
	 * @param URI
	 */
	public void add(String name, String URI) {
		try {
			logger.info(URI);
			// Issue a test query
			String query = URLEncoder.encode("SELECT * WHERE {?s ?p ?o} LIMIT 1", "UTF-8");
			BasicHttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 300);
			ConnManagerParams.setTimeout(httpParams, 300);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
			//http://hc.apache.org/httpclient-3.x/apidocs/org/apache/commons/httpclient/HttpClient.html			
			HttpClient httpClient = new DefaultHttpClient( connManager, httpParams);
			HttpGet httpGet = new HttpGet(URI + "/?query=" + query);
			httpGet.setParams(httpParams);
			httpGet.addHeader("Accept", "application/sparql-results+xml");
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				// Create and add the end point
				EndPoint endpoint = new EndPoint(name, URI);
				listOfEndPoints.add(endpoint);
			}
			entity.consumeContent();
			
		} catch (Exception e) {
			logger.info("Failed");
		}
	}

	/**
	 * @param configFile
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void initFromConfigFile(final String configFile) throws FileNotFoundException, IOException {
		initFromInputStream(new FileInputStream(configFile));
	}

	/**
	 * @param inputStream
	 * @throws IOException
	 */
	public void initFromInputStream(final InputStream inputStream) throws IOException {
		// Clear the current list
		listOfEndPoints.clear();

		Properties config = new Properties();
		config.load(inputStream);
		int index = 0;
		String name = config.getProperty("endpoint." + index + ".name");
		while (name != null) {
			String URI = config.getProperty("endpoint." + index + ".URI");
			add(name, URI);
			index++;
			name = config.getProperty("endpoint." + index + ".name");
		}

		logger.info("Added " + listOfEndPoints.size() + " end points");
	}

	/**
	 * 
	 */
	public void initFromCKAN() {
		// Clear the current list
		listOfEndPoints.clear();

		String URL = "http://ckan.net/api/search/resource?format=api/sparql&all_fields=1&limit=500";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(URL);
		request.setHeader("Accept", "application/json");
		request.setHeader("Accept-Encoding", "gzip");
		try {
			HttpResponse response = (HttpResponse) httpclient.execute(request);
			JsonObject result = JSON.parse(response.getEntity().getContent());
			for (JsonValue entry : result.get("results").getAsArray()) {
				String name = entry.getAsObject().get("package_id").getAsString().value();
				String URI = entry.getAsObject().get("url").getAsString().value();
				add(name, URI);
			}
		} catch (Exception e) {
		}

		logger.info("Loaded " + listOfEndPoints.size() + " end points from ckan.net");
	}
}
