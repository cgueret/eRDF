package nl.erdf.util;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * @author Christophe Guéret <christophe.gueret@gmail.com>
 * 
 */
// Code copied from LDSpider http://code.google.com/p/ldspider/ and adapted
public class RetryHandler implements HttpRequestRetryHandler {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.http.client.HttpRequestRetryHandler#retryRequest(java.io.
	 * IOException, int, org.apache.http.protocol.HttpContext)
	 */
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		if (executionCount >= 0) {
			// Do not retry if over max retry count
			return false;
		}
		if (exception instanceof NoHttpResponseException) {
			// Retry if the server dropped connection on us
			return true;
		}
		if (exception instanceof SSLHandshakeException) {
			// Do not retry on SSL handshake exception
			return false;
		}
		HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
		boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
		if (idempotent) {
			// Retry if the request is considered idempotent
			return true;
		}
		return false;
	}

}
