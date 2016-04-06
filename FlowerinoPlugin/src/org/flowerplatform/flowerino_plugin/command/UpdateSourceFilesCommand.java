package org.flowerplatform.flowerino_plugin.command;

import static org.flowerplatform.flowerino_plugin.FlowerinoPlugin.log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.flowerplatform.flowerino_plugin.FlowerinoPlugin;
import org.flowerplatform.flowerino_plugin.SourceFileDto;
import org.flowerplatform.tiny_http_server.IHttpCommand;

import processing.app.BaseNoGui;
import processing.app.Editor;

/**
 * TODO: This is currently unused; need to see if we really need it; if not => we can remove.
 * @author Claudiu Matei
 *
 */
public class UpdateSourceFilesCommand extends ArrayList<SourceFileDto> implements IHttpCommand, List<SourceFileDto> {
	
	private static final long serialVersionUID = 1L;

	private static final String INO = ".ino";

	private static final String FLOWER_PLATFORM_WORK_INO = "flower-platform-work.ino";

	public Object run() {
		File dir = FlowerinoPlugin.getFlowerPlatformWorkFolder();
		
		//delete all files in work folder
		for (File f : dir.listFiles()) {
			f.delete();
		}
		
		// update source files
		for (SourceFileDto srcFile : this) {
			if (srcFile.getName().endsWith(INO)) {
				srcFile.setName(FLOWER_PLATFORM_WORK_INO);
			}
			File f = new File(dir.getAbsolutePath() + File.separator + srcFile.getName());
			try {
				BaseNoGui.saveFile(srcFile.getContents(), f);
				log("File updated: " + f);
			} catch (IOException e1) {
				log("Error while saving file = " + f, e1);
			}
		}
		
		// reload project
		Editor editor = FlowerinoPlugin.getInstance().getEditor();
	    editor.internalCloseRunner();
		try {
			Method handleOpenInternal = Editor.class.getDeclaredMethod("handleOpenInternal", File.class);
			handleOpenInternal.setAccessible(true);
		    handleOpenInternal.invoke(editor, new File(dir.getAbsolutePath() + File.separator + FLOWER_PLATFORM_WORK_INO));
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e1) {
			log("Error while reloading project", e1);
		}
		log("Sketch reloaded from Flowerino repository");
		
		return null;
	}

}
