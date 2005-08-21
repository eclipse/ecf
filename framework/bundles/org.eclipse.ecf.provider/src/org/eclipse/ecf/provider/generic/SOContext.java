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

/*
 * Created on Dec 6, 2004
 *  
 */
package org.eclipse.ecf.provider.generic;

import java.io.IOException;
import java.util.Map;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IOSGIService;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.IQueueEnqueue;
import org.eclipse.ecf.provider.Trace;

public class SOContext implements ISharedObjectContext {
    static Trace debug = Trace.create("sharedobjectcontext");
    protected SOContainer container = null;
    protected ID sharedObjectID;
    protected ID homeContainerID;
    protected boolean isActive;
    protected Map properties;
    protected IQueueEnqueue queue;

    public SOContext(ID objID, ID homeID, SOContainer cont, Map props,
            IQueueEnqueue queue) {
        super();
        this.sharedObjectID = objID;
        this.homeContainerID = homeID;
        this.container = cont;
        this.properties = props;
        this.queue = queue;
        isActive = true;
    }

    public boolean isActive() {
    	return isActive;
    }
    protected void trace(String msg) {
        if (Trace.ON && debug != null) {
            debug.msg(msg);
        }
    }

    protected void traceDump(String msg, Throwable e) {
        if (Trace.ON && debug != null) {
            debug.dumpStack(e, msg);
        }
    }


    protected synchronized void makeInactive() {
    	isActive = false;
    }

    protected synchronized boolean isInactive() {
        return !isActive;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#getContainerID()
     */
    public synchronized ID getLocalContainerID() {
        return container.getID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#getSharedObjectManager()
     */
    public synchronized ISharedObjectManager getSharedObjectManager() {
        return container.getSharedObjectManager();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#getQueue()
     */
    public synchronized IQueueEnqueue getQueue() {
        return queue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#connect(org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.core.security.IConnectContext)
     */
	public synchronized void connect(ID groupID, IConnectContext joinContext)
	throws ContainerConnectException {
        if (isInactive()) {
        	return;
        } else {
            container.connect(groupID, joinContext);
        }
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#disconnect()
     */
    public synchronized void disconnect() {
        if (isInactive()) {
            trace("leaveGroup() CONTEXT INACTIVE");
            return;
        } else {
            trace("leaveGroup()");
            container.disconnect();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#getConnectedID()
     */
    public synchronized ID getConnectedID() {
        if (isInactive()) {
            return null;
        } else
            return container.getConnectedID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#isGroupManager()
     */
    public synchronized boolean isGroupManager() {
        if (isInactive()) {
            return false;
        } else
            return container.isGroupManager();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#isGroupServer()
     */
    public synchronized boolean isGroupServer() {
        if (isInactive()) {
            return false;
        } else
            return container.isGroupManager();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#getGroupMembership()
     */
    public synchronized ID[] getGroupMemberIDs() {
        if (isInactive()) {
            return new ID[0];
        } else
            return container.getGroupMemberIDs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#sendCreate(org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.core.SharedObjectDescription)
     */
    public synchronized void sendCreate(ID toContainerID,
            SharedObjectDescription sd) throws IOException {
        if (isInactive()) {
            trace("sendCreate("+toContainerID+","+sd+") CONTEXT INACTIVE");
            return;
        } else {
            trace("sendCreate("+toContainerID+","+sd+")");
            container.sendCreate(sharedObjectID, toContainerID, sd);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#sendCreateResponse(org.eclipse.ecf.core.identity.ID, java.lang.Throwable, long)
     */
    public synchronized void sendCreateResponse(ID toContainerID, Throwable throwable, long identifier) throws IOException {
        if (isInactive()) {
            trace("sendCreateResponse("+toContainerID+","+throwable+","+identifier+") CONTEXT INACTIVE");
            return;
        } else {
            trace("sendCreateResponse("+toContainerID+","+throwable+","+identifier+")");
            container.sendCreateResponse(toContainerID, sharedObjectID, throwable, identifier);
        }        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#sendDispose(org.eclipse.ecf.core.identity.ID)
     */
    public synchronized void sendDispose(ID toContainerID) throws IOException {
        if (isInactive()) {
            trace("sendDispose("+toContainerID+") CONTEXT INACTIVE");
            return;
        } else {
            trace("sendDispose("+toContainerID+")");
            container.sendDispose(toContainerID, sharedObjectID);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#sendMessage(org.eclipse.ecf.core.identity.ID,
     *      java.lang.Object)
     */
    public synchronized void sendMessage(ID toContainerID, Object data) throws IOException {
        if (isInactive()) {
            trace("sendMessage("+toContainerID+","+data+") CONTEXT INACTIVE");
            return;
        } else {
            trace("sendMessage("+toContainerID+","+data+") CONTEXT ACTIVE");
            container.sendMessage(toContainerID, sharedObjectID, data);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class clazz) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectContext#getServiceAccess()
     */
    public IOSGIService getServiceAccess() {
        if (isInactive()) {
            trace("getServiceAccess() CONTEXT INACTIVE returning null");
            return null;
        } else {
            trace("getServiceAccess()");
            return container.getOSGIServiceInterface();
        }
    }

	public Namespace getConnectNamespace() {
		return container.getConnectNamespace();
	}

}