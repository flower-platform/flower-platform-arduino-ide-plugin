package org.flowerplatform.flowerino_plugin.util;

import java.lang.reflect.Field;

import processing.app.Editor;

/**
 * Utility class.
 * 
 * @author Andrei Taras
 */
public class Util {
	/**
	 * Hack-ish method that retrieves a private field from the given class instance.
	 * This is used to access stuff from within the main editor.
	 * Please note that all exceptions are forwarded.
	 */
	public static <T>T getPrivateField(Class clazz, Object instance, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		Field field = clazz.getDeclaredField(fieldName); 
		field.setAccessible(true);
		
		return (T)field.get(instance);
	}
	
	public static <T>void setPrivateField(Class clazz, Object instance, String fieldName, T newValue) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		
		field.set(instance, newValue);
	}
	
	/**
	 * Reads the status field from the given editor instance.
	 * @param editor
	 * @return
	 */
	public static EditorStatus readStatus(Editor editor) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		processing.app.EditorStatus editorStatus = Util.getPrivateField(Editor.class, editor, "status");

		EditorStatus result = new EditorStatus();
		
		result.ERR = Util.<Integer>getPrivateField(processing.app.EditorStatus.class, processing.app.EditorStatus.class, "ERR");
		result.status = Util.getPrivateField(processing.app.EditorStatus.class, editorStatus, "mode");
		result.message = Util.getPrivateField(processing.app.EditorStatus.class, editorStatus, "message");

		return result;
	}
	
	public static class EditorStatus {
		/**
		 * The actual value that represents an error (i.e. when status == ERR, then we've got an error).
		 */
		public int ERR;
		
		public int status;
		public String message;
	}
}
