package org.flowerplatform.flowerino_plugin.library_manager.compatibility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.flowerplatform.flowerino_plugin.FlowerPlatformPlugin;

import processing.app.BaseNoGui;
import cc.arduino.contributions.libraries.ContributedLibrary;
import cc.arduino.contributions.libraries.LibrariesIndexer;
import cc.arduino.contributions.libraries.LibraryInstaller;

/**
 * For v < v1.6.6.
 * 
 * @author Cristian Spiescu
 */
public class LibraryInstallerWrapperPre166 extends AbstractLibraryInstallerWrapper {

	protected Method remove;
	protected Method install;
	
	public static LibrariesIndexer librariesIndexer;
	
	public LibraryInstallerWrapperPre166() {
		super();
		try {
			remove = installer.getClass().getDeclaredMethod("remove", ContributedLibrary.class);
			install = installer.getClass().getDeclaredMethod("install", ContributedLibrary.class, ContributedLibrary.class);
		} catch (NoSuchMethodException | SecurityException e) {
			FlowerPlatformPlugin.log("Cannot delegate to LibraryInstaller", e);
		}
	}

	@Override
	protected void initLibraryInstaller() {
		try {
			Field liField = BaseNoGui.class.getDeclaredField("librariesIndexer");
			liField.setAccessible(true);
			librariesIndexer = (LibrariesIndexer) liField.get(null);
			
			installer = new LibraryInstaller(librariesIndexer, BaseNoGui.getPlatform());
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			FlowerPlatformPlugin.log("Cannot instantiate LibraryInstaller", e);
		}
	}

	@Override
	public void remove(ContributedLibrary lib) throws Exception {
		remove.invoke(installer, lib);
	}

	@Override
	public void install(ContributedLibrary lib, ContributedLibrary replacedLib) throws Exception {
		install.invoke(installer, lib, replacedLib);
		FlowerPlatformPlugin.log("Please restart Arduino IDE, so that the installed lib is taken into account.");
	}

}
