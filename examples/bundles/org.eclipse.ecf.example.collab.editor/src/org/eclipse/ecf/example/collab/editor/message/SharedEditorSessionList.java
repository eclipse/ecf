package org.eclipse.ecf.example.collab.editor.message;

import java.io.Serializable;
import java.util.List;

public class SharedEditorSessionList implements Serializable {
	private static final long serialVersionUID = 4337027955521207775L;
	private List sessionNames;
	
	public SharedEditorSessionList(List names) {
		sessionNames = names;
	}
	
	public List getNames() {
		return sessionNames;
	}
}
