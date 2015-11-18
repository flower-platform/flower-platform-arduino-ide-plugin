package org.flowerplatform.flowerino_plugin.library_manager;

import java.util.Map;

import processing.app.packages.UserLibrary;

/**
 * @author Cristian Spiescu
 */
public class RequiredLibraryWrapper extends UserLibrary {
	private Map<String, Object> requiredLibrary;

	public RequiredLibraryWrapper(Map<String, Object> requiredLibrary) {
		super();
		this.requiredLibrary = requiredLibrary;
	}
	
	@Override
	public String getUrl() {
		return (String) requiredLibrary.get("url");
	}

	@Override
	public String getArchiveFileName() {
		return getName() + ".zip";
	}

	@Override
	public String getName() {
		return (String) requiredLibrary.get("name");
	}
}
