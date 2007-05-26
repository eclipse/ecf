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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.example.collab.editor.Activator;
import org.eclipse.ecf.example.collab.editor.listeners.EditorListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.DocumentProviderRegistry;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * This action is used to initiate a shared session.
 * 
 * @author kgilmer
 */
public class InitiateSharedSessionAction extends Action implements
		IObjectActionDelegate {

	private IWorkbenchPart targetPart;
	private IFile file;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		targetPart.getSite().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbenchWindow window = targetPart.getSite()
							.getWorkbenchWindow();
					IWorkbench workbench = window.getWorkbench();
					IEditorDescriptor editorDescriptor = workbench
							.getEditorRegistry().getDefaultEditor(
									file.getName());

					// There is no direct file type association. Use the
					// default.
					if (editorDescriptor == null) {
						editorDescriptor = workbench.getEditorRegistry()
								.findEditor("org.eclipse.ui.DefaultTextEditor");
					}

					if (editorDescriptor == null) {
						// Give up, can't get an editor.
						Activator
								.getDefault()
								.getLog()
								.log(
										new Status(
												IStatus.ERROR,
												Activator.PLUGIN_ID,
												0,
												"Failed to get editor for file.  Aborting shared editing session.",
												null));
						return;
					}

					// Open the default editor for the selected file.
					IEditorPart editorPart = window.getActivePage()
							.openEditor(new FileEditorInput(file),
									editorDescriptor.getId());

					// Create ECF container and begin sharing.
					if (editorPart instanceof AbstractTextEditor) {
						IEditorInput editorInput = editorPart.getEditorInput();
						IDocumentProvider dp = DocumentProviderRegistry
								.getDefault().getDocumentProvider(editorInput);
						AbstractTextEditor textEditor = (AbstractTextEditor) editorPart;

						IDocument document = dp.getDocument(editorPart
								.getEditorInput());

						if (document != null) {
							EditorListener listener = new EditorListener(
									document, textEditor, true);
							document.addDocumentListener(listener);
						} else {
							if (dp instanceof TextFileDocumentProvider) {
								((TextFileDocumentProvider) dp)
										.connect(editorPart.getEditorInput());
								document = ((TextFileDocumentProvider) dp)
										.getDocument(editorPart
												.getEditorInput());

								if (document != null) {
									EditorListener listener = new EditorListener(
											document, textEditor, true);
									document.addDocumentListener(listener);
									return;
								}
							}

							Activator
									.getDefault()
									.getLog()
									.log(
											new Status(
													IStatus.ERROR,
													Activator.PLUGIN_ID,
													0,
													"Unable to get reference to editor's document.  Shared session not created.",
													null));
						}
					}
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(
							new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, e
									.getLocalizedMessage(), e));
				}
			}
		});
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) selection;
			Object obj = iss.getFirstElement();
			if (obj instanceof IFile) {
				file = (IFile) obj;
			} else if (obj instanceof IAdaptable) {
				file = (IFile) ((IAdaptable) obj).getAdapter(IFile.class);
			}
		}
	}

}
