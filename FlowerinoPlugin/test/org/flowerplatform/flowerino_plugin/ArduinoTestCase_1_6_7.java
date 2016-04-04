package org.flowerplatform.flowerino_plugin;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	})



public class ArduinoTestCase_1_6_7 {
	public static final String arduinoPropertiesFile = "arduino_1.6.7.properties";
	static String buildExec(String basePath) {
		StringBuilder exec = new StringBuilder();
		
		exec.append("\"" + basePath + "\\java\\bin\\javaw.exe\"");
		exec.append(" -splash:\"" + basePath + "\\lib\\splash.png\"");
		exec.append(" -Dsun.java2d.d3d=false");
		exec.append(" -DAPP_DIR=\"" + basePath + "\"");
		exec.append(" -Xms128M -Xmx512M");
		exec.append(" -classpath \"");
		String libPath = basePath + "\\lib\\";
		
		
		
		File folder = new File(basePath + "//lib");
		FilenameFilter jarFilter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".jar");
		    }};
		for (File file : folder.listFiles(jarFilter)) {
		    if (file.isFile()) {
		        exec.append(libPath + file.getName() +";");
		    }
		}
		
		exec.append("\" -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y processing.app.Base");
		
		return exec.toString();
		
	}
	
	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {
		InputStream propsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(arduinoPropertiesFile);
		Properties props = new Properties();
		props.load(propsStream);
		String basePath = props.getProperty("basePath");
		System.out.println(buildExec(basePath));
		
		
		Process p=Runtime.getRuntime().exec("D:\\Arduino_IDEs\\arduino-1.6.7\\java\\bin\\javaw.exe"); 
		p.waitFor();
//		Scanner in = null;
//		String path = null;
//		try {
//			in = new Scanner(new FileReader("test/arduino_1.6.7.properties"));
//			path = in.nextLine();
//			
//		} catch (FileNotFoundException e) {
//			throw new RuntimeException(arduinoPropertiesFile + " not found");
//		} finally {
//		in.close();
//		}
//		System.out.println(path);
	}
	
	@AfterClass
    public static void tearDown() {
    	System.out.println("tearing down");
    }

}
