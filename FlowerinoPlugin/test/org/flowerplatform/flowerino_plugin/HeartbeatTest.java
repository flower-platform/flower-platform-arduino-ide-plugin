package org.flowerplatform.flowerino_plugin;

import org.flowerplatform.flowerino_plugin.command.HeartbeatCommand;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test if HeartBeat command returns STATUS_OK
 * @author Silviu Negoita
 *
 */
public class HeartbeatTest {
	
	@Test
	public void heartbeat() {
		HeartbeatCommand hCommand = new HeartbeatCommand();
		Status result = (Status) hCommand.run();
		
		assertEquals(result.getMessage(), HeartbeatCommand.STATUS_OK);
	}
}
