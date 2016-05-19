package org.flowerplatform.flowerino_plugin;

import org.flowerplatform.flowerino_plugin.command.UploadToBoardCommand;
import org.flowerplatform.tiny_http_server.HttpCommandException;
import org.junit.Test;

/**
 * This test should always fail due to missing board
 * @author Silviu Negoita
 *
 */
public class UploadToBoardFailTest {
	
	@Test(expected=HttpCommandException.class)
	public void heartbeat() throws HttpCommandException {
		UploadToBoardCommand commandTest = new UploadToBoardCommand();
		// this command should fail
		commandTest.run();
		
	}
}
