package org.flowerplatform.flowerino_plugin;

/**
 * Represents the status of the application. 
 * Currently doesn't do much apart from providing the status as a simple text; but can be extended in the 
 * future to provide a more complex status.
 * 
 * @author Andrei Taras
 */
public class Status {
	private String message;

	public Status() {
	}
	public Status(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
