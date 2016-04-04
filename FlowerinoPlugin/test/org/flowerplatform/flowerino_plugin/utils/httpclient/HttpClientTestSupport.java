package org.flowerplatform.flowerino_plugin.utils.httpclient;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Helper class for tests making http calls; this should be used only for
 * running tests.
 * 
 * @author Andrei Taras
 */
public class HttpClientTestSupport {

	private String baseUrl;
	private HttpClient httpClient;
	
	public HttpClientTestSupport(String baseUrl) {
		this.baseUrl = prepareBaseUrl(baseUrl);
		
		init();
	}
	
	private static String prepareBaseUrl(String baseUrl) {
		if (baseUrl.endsWith("/")) {
			return baseUrl;
		} else {
			return baseUrl + "/";
		}
	}
	
	private void init() {
		httpClient = new HttpClient();
	}
	
	/**
	 * Executes a POST command, reads result, and passes it back to the caller as String.
	 */
	public HttpTestResponse postToRelativeUrl(String relativeUrl, String postData) throws HttpTestException {
		String url = baseUrl + relativeUrl;
		PostMethod method = new PostMethod(url);
		
		try { 
			// Execute the method.
			int statusCode = httpClient.executeMethod(method);
			
			// Read the response body.
			byte[] responseBody = method.getResponseBody();

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary
			// data
			return new HttpTestResponse(statusCode, new String(responseBody));
		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Fatal transport error: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Release the connection.
			method.releaseConnection();
		}
		
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		HttpClientTestSupport testClient = new HttpClientTestSupport("http://localhost:9000");

		System.out.println("---" + testClient.postToRelativeUrl("compile", null));
	}
}
