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
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.ecf.example.collab.CollabClient;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class URIClientConnectAction implements IWorkbenchWindowActionDelegate {
    
    protected CollabClient client = null;

    protected String containerType = null;
    protected String uri = null;
    protected String nickname = null;
    protected Object data = null;
    protected IResource project = null;
    protected String projectName = null;

    public URIClientConnectAction() {
    	client = CollabClient.getDefault();
    }
    public URIClientConnectAction(String containerType, String uri, String nickname, Object data, IResource project) {
    	this();
    	this.containerType = containerType;
    	this.uri = uri;
    	this.nickname = nickname;
        this.data = data;
        setProject(project);
    }
    public void setProject(IResource project) {
        this.project = project;
        projectName = CollabClient.getNameForResource(project);
    }
    public class ClientMultiStatus extends MultiStatus {

		public ClientMultiStatus(String pluginId, int code, IStatus[] newChildren, String message, Throwable exception) {
			super(pluginId, code, newChildren, message, exception);
		}
		public ClientMultiStatus(String pluginId, int code, String message, Throwable exception) {
			super(pluginId, code, message, exception);
		}
    }
    protected void showExceptionInMultiStatus(int code, MultiStatus status, Throwable t) {
    	String msg = t.getMessage();
    	status.add(new Status(IStatus.ERROR,ClientPlugin.PLUGIN_ID,code++,msg,null));
    	StackTraceElement [] stack = t.getStackTrace();
    	for(int i=0; i < stack.length; i++) {
    		status.add(new Status(IStatus.ERROR,ClientPlugin.PLUGIN_ID,code++,"     "+stack[i],null));
    	}
    	Throwable cause = t.getCause();
    	if (cause != null) {
    		status.add(new Status(IStatus.ERROR,ClientPlugin.PLUGIN_ID,code++,"Caused By: ",null));
    		showExceptionInMultiStatus(code,status,cause);
    	}
    }
	public class ClientConnectJob extends Job {
        public ClientConnectJob(String name) {
            super(name);
        }
        public IStatus run(IProgressMonitor pm) {
        	String failMsg = "Connect to "+uri+" failed";
        	ClientMultiStatus status = new ClientMultiStatus(ClientPlugin.PLUGIN_ID, 0,
                    failMsg, null);
            try {
                client.createAndConnectClient(containerType, uri,nickname, data,project);
                return status;
            } catch (Exception e) {
            	showExceptionInMultiStatus(15555,status,e);
                return status;
            }
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