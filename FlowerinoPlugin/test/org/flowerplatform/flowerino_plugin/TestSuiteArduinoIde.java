package org.flowerplatform.flowerino_plugin;

import org.flowerplatform.flowerino_plugin.utils.ArduinoIdeWrapper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.googlecode.junittoolbox.SuiteClasses;
import com.googlecode.junittoolbox.WildcardPatternSuite;

@RunWith(WildcardPatternSuite.class)
@SuiteClasses({"**/*.class", "!*TestSuiteArduinoIde.class"})
public class TestSuiteArduinoIde {
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
