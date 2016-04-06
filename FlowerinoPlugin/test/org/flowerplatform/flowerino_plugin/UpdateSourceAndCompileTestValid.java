package org.flowerplatform.flowerino_plugin;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.flowerplatform.flowerino_plugin.command.UpdateSourceFilesAndCompileCommand;
import org.flowerplatform.tiny_http_server.HttpCommandException;
import org.junit.Test;

/**
 * Should pass when valid source is given.
 * @author Silviu Negoita
 *
 */
public class UpdateSourceAndCompileTestValid {

	@Test
	public void test() throws HttpCommandException, IOException {
		String fileName = "simpleCompile.ino";
		UpdateSourceFilesAndCompileCommand test = new UpdateSourceFilesAndCompileCommand();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(fileName);
		String content = IOUtils.toString(is, "UTF-8");
		SourceFileDto fileToTest = new SourceFileDto();
		fileToTest.setContents(content);
		fileToTest.setName(fileName);
		test.add(fileToTest);
		test.run();
	}

}
