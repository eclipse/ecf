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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.ecf.example.collab.CollabClient;
import org.eclipse.ecf.example.collab.start.AccountStart;
import org.eclipse.ecf.example.collab.start.ConnectionDetails;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class URIClientConnectAction implements IWorkbenchWindowActionDelegate {
    
    protected CollabClient client = null;

    protected String containerType = null;
    protected String uri = null;
    protected String nickname = null;
    protected String password = null;
    protected IResource project = null;
    protected String projectName = null;
    protected boolean autoLogin = false;
    
    public URIClientConnectAction() {
    	client = CollabClient.getDefault();
    }
    public URIClientConnectAction(String containerType, String uri, String nickname, String password, IResource project, boolean autoLoginFlag) {
    	this();
    	this.containerType = containerType;
    	this.uri = uri;
    	this.nickname = nickname;
        this.password = password;
        this.autoLogin = autoLoginFlag;
        setProject(project);
    }
    public void setProject(IResource project) {
        this.project = project;
        projectName = CollabClient.getNameForResource(project);
    }
	public class ClientConnectJob extends Job {
        public ClientConnectJob(String name) {
            super(name);
        }
        public IStatus run(IProgressMonitor pm) {
            try {
                client.createAndConnectClient(containerType, uri,nickname, password,project);
                if (autoLogin) saveAutoLoginInfo();
                return new Status(IStatus.OK,ClientPlugin.getDefault().getBundle().getSymbolicName(),15000,"Connected",null);
            } catch (ContainerConnectException e) {
            	removeAutoLoginInfo();
            	Throwable c = e.getCause();
            	Status s1 = null;
            	if (c != null) {
            		s1 = new Status(IStatus.ERROR,ClientPlugin.getDefault().getBundle().getSymbolicName(),15551,"Could not connect to "+uri+"\n\n"+c.getMessage()+"\nSee stack trace in Error Log",c);
            		return s1;
            	} else return new Status(IStatus.ERROR,ClientPlugin.getDefault().getBundle().getSymbolicName(),15551,"Could not connect to "+uri+"\n\n"+e.getMessage()+"\nSee stack trace in Error Log",e);
            } catch (Exception e) {
            	return new Status(IStatus.ERROR,ClientPlugin.getDefault().getBundle().getSymbolicName(),15555,"Could not connect to "+uri+"\n\n"+e.getMessage()+"\nSee stack trace in Error Log",e);
            }
        }
		private void saveAutoLoginInfo() {
			AccountStart as = new AccountStart();
			as.addConnectionDetails(new ConnectionDetails(containerType,uri,nickname,password));
			as.saveConnectionDetailsToPreferenceStore();
		}    
		private void removeAutoLoginInfo() {
			AccountStart as = new AccountStart();
			as.removeConnectionDetails(new ConnectionDetails(containerType,uri,nickname,password));
		}
    }
	public void run(IAction action) {
        ClientConnectJob clientConnect = new ClientConnectJob("Connect for "+projectName);
        clientConnect.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {

	}

	public void init(IWorkbenchWindow window) {
	}
}