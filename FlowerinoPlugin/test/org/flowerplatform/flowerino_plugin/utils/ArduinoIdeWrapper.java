package org.flowerplatform.flowerino_plugin.utils;

import processing.app.Base;

/**
 * Used for running tests only; this is main entry point for actually launching the
 * ArduinoIDE who'se jars are found on the current classpath.
 * 
 * @author Andrei Taras
 */
public class ArduinoIdeWrapper {
	public static void launchArduinoIde() {
		//processing.app.Base
		try {
			Base.main(new String[] {});
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public static void stopArduinoIde() {
	}
}
