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
package org.eclipse.ecf.example.collab.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.ecf.example.collab.actions.ClientConnectAction;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

public class JoinGroupWizard extends Wizard {
    JoinGroupWizardPage mainPage;
    private IProject project;
    private IWorkbench workbench;

    public JoinGroupWizard(IProject project, IWorkbench workbench) {
        super();
        this.project = project;
        this.workbench = workbench;
    }

    protected ISchedulingRule getSchedulingRule() {
        return project;
    }
    public void addPages() {
        super.addPages();
        mainPage = new JoinGroupWizardPage();
        addPage(mainPage);
    }

    public boolean performFinish() {
        try {
            finishPage(new NullProgressMonitor());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void finishPage(final IProgressMonitor monitor)
            throws InterruptedException, CoreException {
        
        ClientConnectAction client = null;
        String groupName = mainPage.getJoinGroupText();
        String nickName = mainPage.getNicknameText();
        String containerType = mainPage.getContainerType();
        String password = mainPage.getPasswordText();
        try {
            ID groupID = IDFactory.makeStringID(groupName);
            client = new ClientConnectAction();
            client.setProject(project);
            client.setUsername(nickName);
            client.setTargetID(groupID);
            client.setContainerType(containerType);
            client.setData(password);
            client.run(null);
        } catch (Exception e) {
            String id = ClientPlugin.PLUGIN_ID;
            throw new CoreException(new Status(Status.ERROR, id, 100, "Could not connect to "+groupName, e));
        }
    }
}
