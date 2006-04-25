package org.eclipse.ecf.example.collab.editor.message;

import java.io.Serializable;

public class EditorChangeMessage implements Serializable {
	private static final long serialVersionUID = -8142516068285829708L;
	private String document;
	
	public EditorChangeMessage() {
		
	}
	
	public EditorChangeMessage(String document) {
		this.document = document;
	}

	public String toString() {
		
		return "Text Length: " + document.length();
	}

	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}
	
	
}
