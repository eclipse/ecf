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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;

public class ProjectPopupLocalhostAction implements IObjectActionDelegate {

	/**
	 * Constructor for Action1.
	 */
	public ProjectPopupLocalhostAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
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
				}
				if (obj instanceof IProject) {
					project = (IProject) obj;
				}
			}
		}
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
        ClientConnectAction caction = new ClientConnectAction();
        caction.setProject(project);
        caction.run(action);
	}

    IProject project;
    
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
