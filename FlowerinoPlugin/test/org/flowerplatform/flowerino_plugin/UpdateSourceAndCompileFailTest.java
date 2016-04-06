package org.flowerplatform.flowerino_plugin;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.flowerplatform.flowerino_plugin.command.UpdateSourceFilesAndCompileCommand;
import org.flowerplatform.tiny_http_server.HttpCommandException;
import org.junit.Test;
/**
 * Should fail when invalid source given.
 * @author Silviu Negoita
 *
 */
public class UpdateSourceAndCompileFailTest {

	@Test(expected=HttpCommandException.class)
	public void test() throws HttpCommandException, IOException {
		String fileName = "simpleNotCompile.ino";
		UpdateSourceFilesAndCompileCommand test = new UpdateSourceFilesAndCompileCommand();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(fileName);
		String content = IOUtils.toString(is, "UTF-8");
		SourceFileDto fileToTest = new SourceFileDto();
		fileToTest.setContents(content);
		fileToTest.setName(fileName);
		test.add(fileToTest);
		
		// This line should throw HttpCommandException.
		test.run();
	}

}
