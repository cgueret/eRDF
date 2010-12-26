package nl.erdf.main;

import java.io.IOException;

import nl.erdf.datalayer.sparql.Directory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openjena.atlas.json.JSON;
import org.openjena.atlas.json.JsonObject;
import org.openjena.atlas.json.JsonValue;

public class Test {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static void main(String[] args) throws ClientProtocolException, IOException {
		Directory d = new Directory();
		String URL = "http://ckan.net/api/search/resource?format=api/sparql&all_fields=1&limit=500";
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet request = new HttpGet(URL);
		request.setHeader("Accept", "application/json");
		request.setHeader("Accept-Encoding", "gzip");
		HttpResponse response = (HttpResponse) httpclient.execute(request);
		JsonObject result = JSON.parse(response.getEntity().getContent());
		for (JsonValue entry : result.get("results").getAsArray()) {
			String name = entry.getAsObject().get("package_id").getAsString().value();
			String URI = entry.getAsObject().get("url").getAsString().value();
			d.add(name, URI);
		}
		System.out.println(d.endPoints().size());
	}
}
