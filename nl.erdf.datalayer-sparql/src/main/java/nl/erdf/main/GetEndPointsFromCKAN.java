package nl.erdf.main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nl.erdf.datalayer.sparql.orig.Directory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openjena.atlas.json.JSON;
import org.openjena.atlas.json.JsonArray;
import org.openjena.atlas.json.JsonObject;
import org.openjena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cgueret
 * 
 */
public class GetEndPointsFromCKAN {
	/** Logger instance */
	private static final Logger logger = LoggerFactory.getLogger(GetEndPointsFromCKAN.class);

	/**
	 * Open a ReST query and parse the results as JSON
	 * 
	 * @param query
	 * @return
	 */
	static JsonObject execQuery(String query) {
		try {
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpGet request = new HttpGet(query);
			request.setHeader("Accept", "application/json");
			request.setHeader("Accept-Encoding", "gzip");
			HttpResponse response = (HttpResponse) httpclient.execute(request);
			return JSON.parse(response.getEntity().getContent());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws ClientProtocolException, IOException, InterruptedException {
		String URL;

		// Create a directory
		Directory d = new Directory();

		// Send a query to CKAN
		URL = "http://ckan.net/api/search/resource?format=api/sparql&all_fields=1&limit=500";
		JsonArray results = execQuery(URL).get("results").getAsArray();
		logger.info("Received " + results.size() + " SPARQL end points from ckan.net");

		// Iterate over all the packages
		for (JsonValue entry : results) {
			// Get some basic information
			String id = entry.getAsObject().get("package_id").getAsString().value();
			String URI = entry.getAsObject().get("url").getAsString().value();

			// Test if the end point is alive
			EndPointTester t = new EndPointTester(URI);
			t.setDaemon(true);
			long start = System.currentTimeMillis();
			t.start();
			t.join(200);
			long delay = System.currentTimeMillis() - start;
			if (t.isAlive()) {
				t.stopQuery();
				t.interrupt();
			}
			if (t.isValid()) {
				// Get extra information
				URL = "http://ckan.net/api/search/package?all_fields=1&id=" + id;
				JsonValue info = execQuery(URL).get("results").getAsArray().get(0);
				String title = info.getAsObject().get("title").getAsString().value();
				String name = info.getAsObject().get("name").getAsString().value();
				String label = title + " (" + name + ")";

				// Print status and save end point
				logger.info("Validated " + URI + " as \"" + label + "\" [" + delay + " ms]");
				d.add(label, URI);
			} else {
				logger.warn("Failed on " + URI + " [" + t.getError() + "]");
			}
		}

		// Print some information and save
		logger.info("Directory contains " + d.endPoints().size() + " end points.");
		OutputStream os = new FileOutputStream("ckan-endpoints.csv");
		d.writeTo(os);
		os.close();
		
		// Close the directory
		d.close();
	}
}
