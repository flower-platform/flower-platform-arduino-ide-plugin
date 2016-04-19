package org.flowerplatform.flowerino_plugin.library_manager.compatibility;

import cc.arduino.contributions.libraries.ContributedLibrary;
import cc.arduino.contributions.libraries.LibraryInstaller;

/**
 * Needed because between 1.6.5 and 1.6.6 the signature was modified
 * 
 * @author Cristian Spiescu
 */
public abstract class AbstractLibraryInstallerWrapper {
	
	protected LibraryInstaller installer;
	
	abstract public void remove(ContributedLibrary lib) throws Exception;
	abstract public void install(ContributedLibrary lib, ContributedLibrary replacedLib) throws Exception;
	
	public AbstractLibraryInstallerWrapper() {
		super();
		initLibraryInstaller();
	}
	
	abstract protected void initLibraryInstaller();
	
}
