/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.example.collab.editor.message;

import java.io.Serializable;

/**
 * This message is passed when a document is changed.  Currently the ENTIRE document
 * is passed upon each successive modification, from the modifiers to all other peers
 * in the shared container.
 * 
 * TODO: provide a more efficient way of passing IDocument model changes.
 * 
 * @author kgilmer
 *
 */
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
