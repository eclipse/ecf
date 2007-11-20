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

package org.eclipse.ecf.docshare.menu;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.docshare.DocShare;
import org.eclipse.ecf.internal.docshare.Activator;
import org.eclipse.ecf.internal.docshare.Messages;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuHandler;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
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

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		final IContainer container = (IContainer) getRosterEntry().getRoster().getPresenceContainerAdapter().getAdapter(IContainer.class);
		if (container.getConnectedID() == null)
			throw new ExecutionException(Messages.DocShareRosterMenuHandler_ERROR_NOT_CONNECTED);
		final DocShare sender = Activator.getDefault().getDocShare(container.getID());
		if (sender == null)
			throw new ExecutionException(Messages.DocShareRosterMenuHandler_ERROR_NO_SENDER);
		if (sender.isSharing())
			throw new ExecutionException(Messages.DocShareRosterMenuHandler_ERROR_EDITOR_ALREADY_SHARING);
		final ITextEditor textEditor = getTextEditor();
		if (textEditor == null)
			throw new ExecutionException(Messages.DocShareRosterMenuHandler_EXCEPTION_EDITOR_NOT_TEXT);
		final String fileName = getFileName(textEditor);
		if (fileName == null)
			throw new ExecutionException(Messages.DocShareRosterMenuHandler_NO_FILENAME_WITH_CONTENT);
		final IUser user = getRosterEntry().getRoster().getUser();
		sender.startShare(user.getID(), user.getName(), getRosterEntry().getUser().getID(), fileName, textEditor);
		return null;
	}
}
