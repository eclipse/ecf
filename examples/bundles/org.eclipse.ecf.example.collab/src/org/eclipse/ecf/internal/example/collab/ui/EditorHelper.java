/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.example.collab.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject;
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

	public EditorHelper(IWorkbenchWindow window) {
		this.window = window;
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return window;
	}

	public IEditorPart openEditorForFile(IFile file) throws PartInitException {
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

	protected ITextEditor openTextEditorForFile(IFile file)
			throws PartInitException {
		IEditorPart editor = openEditorForFile(file);
		if (editor != null && (editor instanceof ITextEditor)) {
			return (ITextEditor) editor;
		} else
			return null;
	}

	public void openAndSelectForFile(IFile file, int offset, int length)
			throws PartInitException {
		ITextEditor textEditor = openTextEditorForFile(file);
		if (textEditor == null)
			return;
		setTextEditorSelection(textEditor, offset, length);
	}

	protected IMarker createMarkerForFile(IFile file,
			EclipseCollabSharedObject.SharedMarker marker) throws CoreException {
		IMarker m = file
				.createMarker(EclipseCollabSharedObject.SHARED_MARKER_TYPE);
		m.setAttribute(EclipseCollabSharedObject.SHARED_MARKER_KEY, "slewis");
		// m.setAttribute(IMarker.MESSAGE, marker.getMessage());
		m.setAttribute(IMarker.MESSAGE, "hello");
		m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
		Integer offset = marker.getOffset();
		Integer length = marker.getLength();
		int start = ((offset == null) ? 0 : marker.getOffset().intValue());
		m.setAttribute(IMarker.CHAR_START, start);
		int end = start
				+ ((length == null) ? 0 : marker.getOffset().intValue());
		m.setAttribute(IMarker.CHAR_END, end);
		return m;
	}

	public void openAndAddMarkerForFile(IFile file,
			EclipseCollabSharedObject.SharedMarker marker)
			throws PartInitException, CoreException {
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
		createMarkerForFile(file, marker);
	}

	protected void setTextEditorSelection(ITextEditor textEditor, int offset,
			int length) {
		textEditor.selectAndReveal(offset, length);
	}

	protected String getEditorIdForFile(IFile file) {
		IWorkbench wb = getWorkbenchWindow().getWorkbench();
		IEditorRegistry er = wb.getEditorRegistry();
		IEditorDescriptor desc = er.getDefaultEditor(file.getName());
		if (desc != null)
			return desc.getId();
		else
			return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}
}
