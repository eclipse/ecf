package org.eclipse.ecf.example.collab.share;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorHelper {

	IWorkbenchWindow window = null;
	
	protected IWorkbenchWindow getWorkbenchWindow() {
		return window;
	}
	protected IEditorPart openEditorForFile(IFile file) throws PartInitException {
		IWorkbenchPage page = getWorkbenchWindow().getActivePage();
		IEditorInput input = new FileEditorInput(file);
		// try to find an open editor with this input
		IEditorPart part = page.findEditor(input);
		if (part != null) {
			// found one, activate it
			page.activate(part);
		} else {
			// no editor found, open a new one
			String editorId = getEditorIdForFile(file);
			part = page.openEditor(input, editorId);
		}
		return part;
	}
	
	protected ITextEditor openTextEditorForFile(IFile file) throws PartInitException {
		IEditorPart editor = openEditorForFile(file);
		if (editor != null && (editor instanceof ITextEditor)) {
			return (ITextEditor) editor;
		} else return null;
	}
	
	protected void setTextEditorSelection(ITextEditor textEditor, int offset, int length) {
		textEditor.selectAndReveal(offset, length);
	}
	protected String getEditorIdForFile(IFile file) {
		IWorkbench wb = getWorkbenchWindow().getWorkbench();
		IEditorRegistry er = wb.getEditorRegistry();
		IEditorDescriptor desc = er.getDefaultEditor(file.getName());
		if (desc != null) return desc.getId();
		else return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}
}
