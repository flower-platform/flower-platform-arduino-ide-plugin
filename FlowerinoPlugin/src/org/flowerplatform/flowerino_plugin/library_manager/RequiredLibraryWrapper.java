package org.flowerplatform.flowerino_plugin.library_manager;

import static org.flowerplatform.flowerino_plugin.library_manager.compatibility.LibraryInstallerWrapperPre166.librariesIndexer;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.flowerplatform.flowerino_plugin.FlowerPlatformPlugin;
import org.flowerplatform.flowerino_plugin.library_manager.compatibility.LibraryInstallerWrapperPre166;

import cc.arduino.utils.FileHash;
import processing.app.packages.UserLibrary;

/**
 * @author Cristian Spiescu
 */
public class RequiredLibraryWrapper extends UserLibrary {
	private Map<String, Object> requiredLibrary;
	
	private String checksum;
	
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

	/**
	 * For pre 1.6.6, the checksum IS needed (by <code>DownloadableContributionsDownloader.download()</code>). So we calculate it
	 * to make the system happy. Further versions verify if the checksum is present.
	 */
	@Override
	public String getChecksum() {
		if (librariesIndexer != null && checksum == null) {
			try {
				checksum = FileHash.hash(new File(librariesIndexer.getStagingFolder(), getArchiveFileName()), "SHA-256");
			} catch (NoSuchAlgorithmException | IOException e) {
				FlowerPlatformPlugin.log("Cannot calculate checksum", e);
			}
		}
		return checksum;
	}
	
}
