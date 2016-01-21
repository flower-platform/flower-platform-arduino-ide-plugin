package org.flowerplatform.flowerino_plugin.server.command;

import org.flowerplatform.flowerino_plugin.server.IHttpCommand;

/**
 * 
 * @author Claudiu Matei
 *
 */
public class UpdateSourceFilesCommand implements IHttpCommand {
	
	String inoFile;
	
	public String getInoFile() {
		return inoFile;
	}

	public void setInoFile(String inoFile) {
		this.inoFile = inoFile;
	}
	
	public String run() {
		System.out.println(inoFile);
		return "OK";
	}

}
