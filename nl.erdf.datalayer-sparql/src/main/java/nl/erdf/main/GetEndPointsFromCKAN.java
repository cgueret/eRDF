package nl.erdf.main;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import nl.erdf.datalayer.sparql.Directory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
			HttpResponse response = httpclient.execute(request);
			JsonParser parser = new JsonParser();
			return (JsonObject) parser.parse(response.toString());
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
		JsonArray results = execQuery(URL).get("results").getAsJsonArray();
		logger.info("Received " + results.size() + " SPARQL end points from ckan.net");

		// Iterate over all the packages
		for (JsonElement entry : results) {
			// Get some basic information
			String id = entry.getAsJsonObject().get("package_id").getAsString();
			String uri = entry.getAsJsonObject().get("url").getAsString();

			// Test if the end point is alive
			EndPointTester t = new EndPointTester(uri);
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
				JsonElement info = execQuery(URL).get("results").getAsJsonArray().get(0);
				String title = info.getAsJsonObject().get("title").getAsString();
				String name = info.getAsJsonObject().get("name").getAsString();
				String label = title + " (" + name + ")";

				// Print status and save end point
				logger.info("Validated " + uri + " as \"" + label + "\" [" + delay + " ms]");
				d.add(label, URI.create(uri));
			} else {
				logger.warn("Failed on " + uri + " [" + t.getError() + "]");
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
