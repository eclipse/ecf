package org.eclipse.ecf.example.collab.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class ShareEditorAction implements IWorkbenchWindowActionDelegate {

	public ShareEditorAction() {
	}

	public void run(IAction action) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		IEditorPart editorPart = page.getActiveEditor();

		if (editorPart instanceof AbstractTextEditor) {
			IDocumentProvider dp = DocumentProviderRegistry.getDefault()
					.getDocumentProvider(editorPart.getEditorInput());
			AbstractTextEditor textEditor = (AbstractTextEditor) editorPart;

			IDocument document = dp.getDocument(editorPart.getEditorInput());

			if (document != null) {
				EditorListener listener = new EditorListener(document, textEditor);
				document.addDocumentListener(listener);
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}
}