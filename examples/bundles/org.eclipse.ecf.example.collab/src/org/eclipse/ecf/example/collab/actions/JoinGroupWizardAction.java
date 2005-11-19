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
package org.eclipse.ecf.example.collab.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ecf.example.collab.ClientEntry;
import org.eclipse.ecf.example.collab.CollabClient;
import org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject;
import org.eclipse.ecf.example.collab.ui.JoinGroupWizard;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;

public class JoinGroupWizardAction extends ActionDelegate implements
        IObjectActionDelegate {
    
	private static final String CONNECT_PROJECT_MENU_TEXT = "Connect Project...";
	private static final String DISCONNECT_PROJECT_MENU_TEXT = "Disconnect Project";
	IProject project;
    boolean connected = false;
    
    public JoinGroupWizardAction() {
        super();
    }

	protected ClientEntry isConnected(IResource res) {
		if (res == null) return null;
		CollabClient client = CollabClient.getDefault();
		ClientEntry entry = client.isConnected(res,
				CollabClient.GENERIC_CONTAINER_CLIENT_NAME);
		return entry;
	}
	protected void setAction(IAction action, IResource res) {
		if (isConnected(res) != null) {
			action.setText(DISCONNECT_PROJECT_MENU_TEXT);
			connected = true;
		} else {
			action.setText(CONNECT_PROJECT_MENU_TEXT);
			connected = false;
		}
	}
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        project = null;
        Object o = targetPart.getAdapter(IShowInSource.class);
        if (o != null) {
            IShowInSource sis = (IShowInSource) o;
            ShowInContext sc = sis.getShowInContext();
            ISelection s = sc.getSelection();
            if (s instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) s;
                Object obj = ss.getFirstElement();
                if (obj instanceof IJavaProject) {
                    IJavaProject ij = (IJavaProject) obj;
                    project = ij.getProject();
                    setAction(action,project);
                }
                if (obj instanceof IProject) {
                    project = (IProject) obj;
                    setAction(action,project);
                }
            }
        }
    }
    protected IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }
    
    public void run(IAction action) {
    	if (!connected) {
	        JoinGroupWizard wizard = new JoinGroupWizard(project,getWorkbench());
	        // Create the wizard dialog
	        WizardDialog dialog = new WizardDialog
	         (getWorkbench().getActiveWorkbenchWindow().getShell(),wizard);
	        // Open the wizard dialog
	        dialog.open();
    	} else {
    		ClientEntry client = isConnected(project);
    		if (client == null) {
    			connected = false;
    			action.setText(CONNECT_PROJECT_MENU_TEXT);
    		} else {
    			EclipseCollabSharedObject collab = client.getObject();
    			if (collab != null) {
    				collab.chatGUIDestroy();
    			}
    		}
    	}
    }
}
