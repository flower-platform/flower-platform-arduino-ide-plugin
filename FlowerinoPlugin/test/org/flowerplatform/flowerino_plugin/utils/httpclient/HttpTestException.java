package org.flowerplatform.flowerino_plugin.utils.httpclient;

/**
 * Generic umbrella exception for any errors related to HTTP operations during tests.
 * 
 * @author Andrei Taras
 */
public class HttpTestException extends Exception {

	private static final long serialVersionUID = -3981337168541768098L;

	public HttpTestException() {
		super();
	}
	public HttpTestException(String message, Throwable cause) {
		super(message, cause);
	}
	public HttpTestException(String message) {
		super(message);
	}
	public HttpTestException(Throwable cause) {
		super(cause);
	}
}
