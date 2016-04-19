package org.flowerplatform.flowerino_plugin;

import org.flowerplatform.flowerino_plugin.utils.ArduinoIdeWrapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*.class", "!*TestSuiteLauncher.class"})
public class ArduinoIde_v167_TestSuiteLauncher {
	@BeforeClass 
	public static void launchArduinoIde() {
		System.out.println("Starting Arduino ide.");
		ArduinoIdeWrapper.launchArduinoIde();
	}

	@AfterClass
	public static void shutdownArduinoIde() {
		System.out.println("Stopping Arduino ide.");
		ArduinoIdeWrapper.stopArduinoIde();
	}
}
