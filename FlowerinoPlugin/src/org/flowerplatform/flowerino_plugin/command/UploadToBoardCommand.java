package org.flowerplatform.flowerino_plugin.command;

import static org.flowerplatform.flowerino_plugin.FlowerPlatformPlugin.log;

import org.flowerplatform.flowerino_plugin.FlowerPlatformPlugin;
import org.flowerplatform.flowerino_plugin.util.RunnableWithListener;
import org.flowerplatform.flowerino_plugin.util.StartEndListener;
import org.flowerplatform.flowerino_plugin.util.Util;
import org.flowerplatform.tiny_http_server.HttpCommandException;
import org.flowerplatform.tiny_http_server.IHttpCommand;
import org.flowerplatform.tiny_http_server.ReflectionException;

import processing.app.Editor;

/**
 * Triggers the Arduino IDE 's board upload procedure.
 * 
 * @author Andrei Taras
 */
public class UploadToBoardCommand implements IHttpCommand {

	public static final String EXPORT_HANDLER_FIELD_NAME = "exportHandler";
	
	@Override
	public Object run() throws HttpCommandException {
		Editor editor = FlowerPlatformPlugin.getInstance().getEditor();
		
		Runnable originalExportHandler = null;
		UploadHandler uploadHandler = null;
		Runnable newExportHandler = null;
		
		// Make preparations to run the upload.
		try {
			// Store somewhere the original export handler
			originalExportHandler = Util.getPrivateField(Editor.class, editor, EXPORT_HANDLER_FIELD_NAME);
			uploadHandler = new UploadHandler(editor, originalExportHandler);
			newExportHandler = new RunnableWithListener(originalExportHandler, uploadHandler);
			
			Util.setPrivateField(Editor.class, editor, EXPORT_HANDLER_FIELD_NAME, newExportHandler);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e1) {
			log("Error while attempting to invoke upload.", e1);
			throw new ReflectionException("Reflection error.");
		} catch (Throwable th) {
			log("Error while attempting to invoke upload.", th);
			throw new HttpCommandException("Error while attempting to invoke upload.", th);
		}

		// Run the actual upload, but in a different try/catch block.
		try {
			editor.handleExport(false);
		} catch (Throwable th) {
			try {
				Util.setPrivateField(Editor.class, editor, EXPORT_HANDLER_FIELD_NAME, originalExportHandler);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e1) {
				log("Error while attempting to invoke upload.", e1);
				throw new ReflectionException("Reflection error.");
			}
			
			log("Error while attempting to invoke upload.", th);
			throw new HttpCommandException("Error while attempting to invoke upload.", th);
		}
		
		waitForUploadResult(uploadHandler);
		if (!uploadHandler.isFinished()) {
			throw new HttpCommandException("Arduino IDE did not finished uploading in the allotted amount of time.");
		} else {
			if (uploadHandler.error != null) {
				if (uploadHandler.error instanceof HttpCommandException) {
					throw ((HttpCommandException)uploadHandler.error);
				} else {
					throw new HttpCommandException(uploadHandler.error);
				}
			} else {
				// Don't do anything; if upload ok, just return as normal.
				return null;
			}
		}		
	}
	
	/**
	 * Pauses current thread until the upload result becomes available.
	 */
	private void waitForUploadResult(UploadHandler uploadHandler) {
		// Arduino IDE runs the upload on a separate thread; we need to wait until
		// the upload is done, until we can read the results.
		
		// Call wait() in intervals of waitIntervalMilliseconds milliseconds.
		long waitIntervalMilliseconds = 500;
		// Wait for the upload to finish for a maximum of maxWaitMilliseconds milliseconds.
		long maxWaitMilliseconds = 1200 * waitIntervalMilliseconds;
		
		while (maxWaitMilliseconds > 0) {
			try {
				synchronized (this) {
					wait(waitIntervalMilliseconds);
				}
			} catch (InterruptedException ignored) { }
			
			maxWaitMilliseconds -= waitIntervalMilliseconds;
			
			if (uploadHandler.finished) {
				break;
			}
		}
	}
	
	/**
	 * Handler for the events involved in uploading code on the board.
	 * 
	 * @author Andrei Taras
	 */
	private class UploadHandler implements StartEndListener {
		
		/**
		 * We need to keep a reference to the original export handler (i.e. the Arduino IDE's private field, which
		 * holds the code that is going to run when we trigger the upload command).
		 * This should be restored immediately after invoking the end() handler.
		 */
		private Runnable originalExportHandler = null;

		/**
		 * The original editor instance.
		 */
		private Editor editor;
		
		/**
		 * If an error occurred during the processing of either start() or end() handlers, it 
		 * will be kept here.
		 * An error will be kept here also if the status of the editor will be error.
		 */
		private Throwable error;
		
		private boolean finished = false;
		
		public UploadHandler(Editor editor, Runnable originalExportHandler) {
			if (editor == null) {
				throw new NullPointerException("The editor instance cannot be null.");
			}
			this.editor = editor;
			this.originalExportHandler = originalExportHandler;
		}
		
		@Override
		public void start() {
			//Nothing to do when starting to run command; just pass through.		
		}
		@Override
		public void end() {
			try {
				// Restore the original export handler
				Util.setPrivateField(Editor.class, editor, EXPORT_HANDLER_FIELD_NAME, originalExportHandler);
				
				Util.EditorStatus editorStatus = Util.readStatus(editor);
				if (editorStatus.status == editorStatus.ERR) {
					error = new HttpCommandException(editorStatus.message);
				} else {
					// Nothing to do here; the upload was a success; we notify this, by NOT setting any error field.
				}
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e1) {
				error = new ReflectionException("Reflection error.", e1);
			} catch (Throwable th) {
				error = th;
			}
			
			finished = true;
		}

		public boolean isFinished() {
			return finished;
		}
	}
}
