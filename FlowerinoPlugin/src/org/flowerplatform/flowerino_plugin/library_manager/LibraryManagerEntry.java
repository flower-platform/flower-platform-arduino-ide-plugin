package org.flowerplatform.flowerino_plugin.library_manager;

import java.util.Map;

import processing.app.packages.UserLibrary;

/**
 * @author Cristian Spiescu
 */
public class LibraryManagerEntry {
	
	public enum Status { 
		OK("OK"), NEEDS_DOWNLOAD("Needs download"), NEEDS_UPDATE("Needs update"), UNKNOWN("Unknown");
		
		private String label;

		private Status(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
		
	};
	
	public enum Action {
		DOWNLOAD("Download"), DELETE("Delete"), NONE("Nothing to do");
		
		private String label;
	
		private Action(String label) {
			this.label = label;
		}
	
		@Override
		public String toString() {
			return label;
		}
		
	};	
	
	private UserLibrary existingLibrary;
	
	private String[] existingHeaderFiles;
	
	private Map<String, Object> requiredLibrary;
	
	private Status status = Status.UNKNOWN;
	
	private Action action = Action.NONE;
	
	private String name;

	public UserLibrary getExistingLibrary() {
		return existingLibrary;
	}

	public void setExistingLibrary(UserLibrary existingLibrary) {
		this.existingLibrary = existingLibrary;
	}

	public String[] getExistingHeaderFiles() {
		return existingHeaderFiles;
	}

	public void setExistingHeaderFiles(String[] existingHeaderFiles) {
		this.existingHeaderFiles = existingHeaderFiles;
	}

	public Map<String, Object> getRequiredLibrary() {
		return requiredLibrary;
	}

	public void setRequiredLibrary(Map<String, Object> requiredLibrary) {
		this.requiredLibrary = requiredLibrary;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LibraryManagerEntry() {
		super();
	}

	public LibraryManagerEntry(Status status, Action action, String name) {
		super();
		this.status = status;
		this.action = action;
		this.name = name;
	}
	
}
