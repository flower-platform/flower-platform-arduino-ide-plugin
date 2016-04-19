package org.flowerplatform.flowerino_plugin;

/**
 * 
 * @author Claudiu Matei
 *
 */
public class SourceFileDto {

	private String name;
	
	private String contents;

	public SourceFileDto() { 
	}
	
	public SourceFileDto(String name, String contents) {
		super();
		this.name = name;
		this.contents = contents;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
}
