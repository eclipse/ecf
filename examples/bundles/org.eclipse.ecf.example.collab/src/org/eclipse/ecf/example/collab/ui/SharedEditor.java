package org.eclipse.ecf.example.collab.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A skeleton shared editor based on TextEditor.  
 * @author kgilmer
 *
 */
public class SharedEditor extends TextEditor implements IEditorPart {

	protected void doSetInput(IEditorInput input) throws CoreException {		
		super.doSetInput(input);
		
		if (input instanceof FileEditorInput) {
			IFile file = ((FileEditorInput)input).getFile();
			System.out.println("Create EclipseCollabSharedObject for " + file.getFullPath());
		}
		
		
	}
}
