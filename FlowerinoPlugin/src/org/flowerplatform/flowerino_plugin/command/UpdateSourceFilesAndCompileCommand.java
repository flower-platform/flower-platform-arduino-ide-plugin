package org.flowerplatform.flowerino_plugin.command;

import static org.flowerplatform.flowerino_plugin.FlowerinoPlugin.log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.flowerplatform.flowerino_plugin.FlowerinoPlugin;
import org.flowerplatform.flowerino_plugin.SourceFileDto;
import org.flowerplatform.flowerino_plugin.util.RunnableWithListener;
import org.flowerplatform.flowerino_plugin.util.StartEndListener;
import org.flowerplatform.flowerino_plugin.util.Util;
import org.flowerplatform.tiny_http_server.HttpCommandException;
import org.flowerplatform.tiny_http_server.IHttpCommand;
import org.flowerplatform.tiny_http_server.ReflectionException;

import processing.app.BaseNoGui;
import processing.app.Editor;

/**
 * Loads the given files into Arduino IDE, and invokes compile on them.
 * @author Claudiu Matei
 * @author Andrei Taras
 */
public class UpdateSourceFilesAndCompileCommand extends ArrayList<SourceFileDto> implements IHttpCommand {
	private static final long serialVersionUID = -2615036179585950419L;

	private static final String STANDARD_EXTENSION = "ino";
	
	private CompilationResult compilationResult;
	
	/**
	 * Deletes all the files contained in the folder given as parameter.
	 * 
	 * @throws HttpCommandException if any problem occurrs.
	 */
	private static void deleteFilesFromFolder(File dir) throws HttpCommandException {
		for (File f : dir.listFiles()) {
			try {
				if (!(f.delete())) {
					throw new HttpCommandException("Can't delete file " + f.getAbsolutePath());
				}
			} catch (HttpCommandException hce) { 
				throw hce;
			} catch (Throwable th) {
				throw new HttpCommandException( String.format("Error while deleting file %s . Message is \"%s\".", f.getAbsolutePath(), th.getMessage()) );
			}
		}
	}

	/**
	 * Persists the given files on disk, and opens the *.ino file in the editor.
	 */
	private void saveAndOpenInEditor(File dir) {
		// update source files
		for (SourceFileDto srcFile : this) {
			if (srcFile.getName().endsWith("." + STANDARD_EXTENSION)) {
				// The *.ino file needs to have the same name as the folder it is found in.
				srcFile.setName(FlowerinoPlugin.FLOWER_PLATFORM_WORK_FOLDER_NAME + "." + STANDARD_EXTENSION);
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
		    handleOpenInternal.invoke(editor, new File(dir.getAbsolutePath() + File.separator + FlowerinoPlugin.FLOWER_PLATFORM_WORK_FOLDER_NAME + ".ino"));
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e1) {
			log("Reflection error while loading files in editor.", e1);
		}
		log("Sketch reloaded from Flowerino repository");
	}
	
	/**
	 * Invokes the editor's compile function.
	 */
	private void invokeCompile(final CompilationListener compilationListener) throws HttpCommandException {
		final Editor editor = FlowerinoPlugin.getInstance().getEditor();
		
		StartEndListener compilationEndListener = new StartEndListener() {
			@Override
			public void start() {
				// We don't really care to intercept this event, so do nothing here. This is here just in case.
			}
			@Override
			public void end() {
				try {
					convertEditorStatusIntoCompilationResult(editor, compilationListener);
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e1) {
					log("Error while attempting to invoke compile.", e1);
					// Don't forward this exception; just mark the fact that we've got an error,
					// and rely on future processing to notify this error to the caller.
					compilationListener.compilationFailed(new ReflectionException("Reflection error.", e1));
				} catch (Throwable th) {
					log("Error while attempting to invoke compile.", th);
					compilationListener.compilationFailed(new HttpCommandException("General error.", th));
				}
			}
		};

		try {
			RunnableWithListener presentHandlerWrapper = new RunnableWithListener(Util.getPrivateField(Editor.class, editor, "presentHandler"), compilationEndListener);
			RunnableWithListener runHandlerWrapper = new RunnableWithListener(Util.getPrivateField(Editor.class, editor, "runHandler"), compilationEndListener);
			
			editor.handleRun(false, presentHandlerWrapper, runHandlerWrapper);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e1) {
			log("Reflection error while attempting to invoke compile.", e1);
			compilationListener.compilationFailed(new ReflectionException("Reflection error.", e1));
		} catch (Throwable th) {
			log("Compilation error", th);
			compilationListener.compilationFailed(new HttpCommandException("Compilation error : " + th.getMessage()));
		}
	}
	
	@Override
	public Object run() throws HttpCommandException {
		File dir = FlowerinoPlugin.getFlowerPlatformWorkFolder();
		
		// Make sure the working folder is clean (i.e. no unnecessary files)
		deleteFilesFromFolder(dir);

		// Open the INO file in the editor, as if the user selected File->Open
		saveAndOpenInEditor(dir);
		
		// Trigger compilation as if the user clicked on "Verify / Compile" 
		compilationResult = new CompilationResult();
		invokeCompile(new CompilationListener() {
			@Override
			public void compilationSuccessful() {
				compilationResult.finished = true;
			}
			@Override
			public void compilationFailed(HttpCommandException error) {
				compilationResult.finished = true;
				compilationResult.error = error;
			}
		});
		
		// Wait until the compilation result becomes available
		waitForCompilationResult();
		
		if (!compilationResult.finished) {
			throw new HttpCommandException("Arduino IDE did not finished compiling in the allotted amount of time.");
		} else {
			if (compilationResult.error != null) {
				if (compilationResult.error instanceof HttpCommandException) {
					throw ((HttpCommandException)compilationResult.error);
				} else {
					throw new HttpCommandException(compilationResult.error);
				}
			} else {
				// Don't do anything; if compilation ok, just return as normal.
				return null;
			}
		}
	}
	
	/**
	 * Pauses current thread until the compilation result becomes available.
	 */
	private void waitForCompilationResult() {
		// Arduino IDE runs the compilation on a separate thread; we need to wait until
		// the compilation is done, until we can read the results.
		
		// Call wait() in intervals of waitIntervalMilliseconds milliseconds.
		long waitIntervalMilliseconds = 500;
		// Wait for the compilation to finish for a maximum of maxWaitMilliseconds milliseconds.
		long maxWaitMilliseconds = 120 * waitIntervalMilliseconds;
		
		while (maxWaitMilliseconds > 0) {
			try {
				synchronized (this) {
					wait(waitIntervalMilliseconds);
				}
			} catch (InterruptedException ignored) { }
			
			maxWaitMilliseconds -= waitIntervalMilliseconds;
			
			if (compilationResult.finished) {
				break;
			}
		}
	}
	
	/**
	 * Utility function; analyzes the internal field "status" of the Arduino IDE main Editor, and
	 * calls the appropiate {@link CompilationListener#compilationSuccessful()} or
	 * {@link CompilationListener#compilationFailed(HttpCommandException)()} method.
	 */
	private void convertEditorStatusIntoCompilationResult(Editor editor, CompilationListener compilationListener) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		Util.EditorStatus editorStatus = Util.readStatus(editor);
		
		if (editorStatus.status == editorStatus.ERR) {
			compilationListener.compilationFailed(new HttpCommandException(editorStatus.message));
		} else {
			compilationListener.compilationSuccessful();
		}
	}
	
	private static interface CompilationListener {
		void compilationSuccessful();
		void compilationFailed(HttpCommandException error);
	}
	
	private static class CompilationResult {
		public boolean finished;
		public Throwable error;
	}
}
