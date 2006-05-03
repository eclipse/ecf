/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.example.collab.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.ecf.example.collab.ui.ConnectionDialog;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * @author slewis
 * 
 */
public class NewWorkbenchAction extends ActionDelegate implements
		IWorkbenchWindowActionDelegate {

	public void run() {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot();
		URIClientConnectAction action = new URIClientConnectAction();
		action.setProject(resource);
	}

	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot();

		// Open the wizard dialog
		ConnectionDialog dialog = new ConnectionDialog(getWorkbench()
				.getActiveWorkbenchWindow().getShell());

		if (dialog.open() == Dialog.OK) {

			URIClientConnectAction client = null;
			String groupName = dialog.getJoinGroupText();
			String nickName = dialog.getNicknameText();
			String containerType = dialog.getContainerType();
			String password = dialog.getPasswordText();
			boolean autoLoginFlag = dialog.getAutoLoginFlag();
			try {
				client = new URIClientConnectAction(containerType, groupName,
						nickName, password, resource,autoLoginFlag);
				client.run(null);
			} catch (Exception e) {
				String message = "Could not connect to " + groupName + ".";
				ClientPlugin.log(message, e);
				ErrorDialog.openError(getWorkbench().getActiveWorkbenchWindow()
						.getShell(), "Connection Error", message, new Status(
						Status.ERROR, ClientPlugin.PLUGIN_ID, 0, message, e));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
