/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.example.collab.editor.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.ecf.example.collab.editor.Activator;
import org.eclipse.ecf.example.collab.editor.EditorListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * This action is used to initiate a shared session.  
 * 
 * @author kgilmer
 *
 */
public class InitiateSharedSessionAction extends Action implements IObjectActionDelegate, IViewActionDelegate {

	private IFile file;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IWorkbench workbench = Activator.getDefault().getWorkbench();
		final IEditorDescriptor editorDescriptor = workbench.getEditorRegistry().getDefaultEditor(file.getName());

		if (workbench != null) {
			final IWorkbenchPage page = workbench.getWorkbenchWindows()[0].getActivePage();

			workbench.getDisplay().asyncExec(new Runnable() {
				IEditorPart part;

				public void run() {
					try {
						//Open the default editor for the selected file.
						IEditorPart editorPart = page.openEditor(new FileEditorInput(file), editorDescriptor.getId());

						//Create ECF container and begin sharing.
						if (editorPart instanceof AbstractTextEditor) {
							IDocumentProvider dp = DocumentProviderRegistry.getDefault().getDocumentProvider(editorPart.getEditorInput());
							AbstractTextEditor textEditor = (AbstractTextEditor) editorPart;

							IDocument document = dp.getDocument(editorPart.getEditorInput());

							if (document != null) {
								EditorListener listener = new EditorListener(document, textEditor);
								document.addDocumentListener(listener);
							}
						}

					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}

			});
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof TreeSelection) {
			TreeSelection ts = (TreeSelection) selection;

			file = (IFile) ts.getFirstElement();
		}
	}

	public void init(IViewPart view) {
	}

}
