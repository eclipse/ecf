/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.provisional.docshare.menu;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.internal.docshare.Activator;
import org.eclipse.ecf.internal.docshare.Messages;
import org.eclipse.ecf.internal.provisional.docshare.DocShare;
import org.eclipse.ecf.presence.roster.*;
import org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuHandler;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 *
 */
public class DocShareRosterMenuHandler extends AbstractRosterMenuHandler {

	/**
	 * @param entry
	 */
	public DocShareRosterMenuHandler(IRosterEntry entry) {
		super(entry);
	}

	protected ITextEditor getTextEditor() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return null;
		final IWorkbenchWindow ww = workbench.getActiveWorkbenchWindow();
		if (ww == null)
			return null;
		final IWorkbenchPage wp = ww.getActivePage();
		if (wp == null)
			return null;
		final IEditorPart ep = wp.getActiveEditor();
		if (ep instanceof ITextEditor)
			return (ITextEditor) ep;
		return null;
	}

	private String getFileName(IEditorPart editorPart) {
		final IEditorInput input = editorPart.getEditorInput();
		if (input instanceof IFileEditorInput) {
			final IFileEditorInput fei = (IFileEditorInput) input;
			return fei.getName();
		}
		return null;
	}

	private void showErrorMessage(String errorMessage) {
		ErrorDialog.openError(null, Messages.DocShareRosterMenuHandler_DOCSHARE_START_ERROR_TITLE, errorMessage, new Status(IStatus.ERROR, Activator.PLUGIN_ID, errorMessage, null));
	}

	/**
	 * @throws ExecutionException  
	 */
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		IRosterEntry rosterEntry = getRosterEntry();
		if (rosterEntry != null) {
			IRoster roster = rosterEntry.getRoster();
			IRosterManager rosterManager = roster.getPresenceContainerAdapter().getRosterManager();
			final IContainer container = (IContainer) roster.getPresenceContainerAdapter().getAdapter(IContainer.class);
			if (container.getConnectedID() == null)
				showErrorMessage(Messages.DocShareRosterMenuHandler_ERROR_NOT_CONNECTED);
			final DocShare sender = Activator.getDefault().getDocShare(container.getID());
			if (sender == null)
				showErrorMessage(Messages.DocShareRosterMenuHandler_ERROR_NO_SENDER);
			if (sender.isSharing())
				showErrorMessage(Messages.DocShareRosterMenuHandler_ERROR_EDITOR_ALREADY_SHARING);
			final ITextEditor textEditor = getTextEditor();
			if (textEditor == null)
				showErrorMessage(Messages.DocShareRosterMenuHandler_EXCEPTION_EDITOR_NOT_TEXT);
			final String fileName = getFileName(textEditor);
			if (fileName == null)
				showErrorMessage(Messages.DocShareRosterMenuHandler_NO_FILENAME_WITH_CONTENT);
			final IUser user = roster.getUser();
			sender.startShare(rosterManager, user.getID(), user.getName(), rosterEntry.getUser().getID(), fileName, textEditor);
		}
		return null;
	}
}
