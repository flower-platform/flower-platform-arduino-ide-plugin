package org.flowerplatform.flowerino_plugin.command;

import org.flowerplatform.flowerino_plugin.Status;
import org.flowerplatform.tiny_http_server.IHttpCommand;

/**
 * Heartbeat command, allowing a client to verify if the Arduino IDE is available or 
 * not (i.e. started). 
 * 
 * @author Andrei Taras
 */
public class HeartbeatCommand implements IHttpCommand {
	
	public static final String STATUS_OK = "Ok";
	
	@Override
	public Object run() {
		//Don't actually do anything, but return a simple status.
		return new Status(STATUS_OK);
	}
}
