/*
 * Created on Dec 6, 2004
 *
 */
package org.eclipse.ecf.internal.impl.standalone;

import java.io.IOException;
import java.util.Map;

import org.eclipse.ecf.core.IOSGIService;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectContainerJoinException;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.QueueEnqueue;

public class StandaloneContext implements ISharedObjectContext {

    StandaloneContainer container = null;
    ID sharedObjectID;
    ID homeContainerID;
    boolean isActive;
    Map properties;
    QueueEnqueue queue;

    public StandaloneContext(ID objID, ID homeID, StandaloneContainer cont, Map props, QueueEnqueue queue) {
        super();
        this.sharedObjectID = objID;
        this.homeContainerID = homeID;
        this.container = cont;
        this.properties = props;
        this.queue = queue;
    }
    protected synchronized void makeInactive() {
        container = null;
        properties = null;
        queue = null;
    }
    protected synchronized boolean isInactive() {
        return (container == null);
    }
    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#getContainerID()
     */
    public synchronized ID getLocalContainerID() {
        if (isInactive()) {
            return null;
        } return container.getConfig().getID();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#getSharedObjectManager()
     */
    public synchronized ISharedObjectManager getSharedObjectManager() {
        if (isInactive()) {
            return null;
        } return container.getSharedObjectManager();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#getQueue()
     */
    public synchronized QueueEnqueue getQueue() {
        if (isInactive()) {
            return null;
        } return queue;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#joinGroup(org.eclipse.ecf.core.identity.ID, java.lang.Object)
     */
    public synchronized void joinGroup(ID groupID, Object loginData)
            throws SharedObjectContainerJoinException {
        if (isInactive()) {
            return;
        } else container.joinGroup(groupID,loginData);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#leaveGroup()
     */
    public synchronized void leaveGroup() {
        if (isInactive()) {
            return;
        } else container.leaveGroup();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#getGroupID()
     */
    public synchronized ID getGroupID() {
        if (isInactive()) {
            return null;
        } else return container.getGroupID();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#isGroupManager()
     */
    public synchronized boolean isGroupManager() {
        if (isInactive()) {
            return false;
        } else return container.isGroupManager();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#isGroupServer()
     */
    public synchronized boolean isGroupServer() {
        if (isInactive()) {
            return false;
        } else return container.isGroupManager();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#getGroupMembership()
     */
    public synchronized ID[] getGroupMemberIDs() {
        if (isInactive()) {
            return null;
        } else return container.getGroupMemberIDs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#sendCreate(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.SharedObjectDescription)
     */
    public synchronized void sendCreate(ID toContainerID, SharedObjectDescription sd)
            throws IOException {
        if (isInactive()) {
            return;
        } else {
            container.sendCreate(sharedObjectID,toContainerID,sd); 
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#sendDispose(org.eclipse.ecf.core.identity.ID)
     */
    public synchronized void sendDispose(ID toContainerID) throws IOException {
        if (isInactive()) {
            return;
        } else {
            container.sendDispose(sharedObjectID,toContainerID); 
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#sendMessage(org.eclipse.ecf.core.identity.ID, java.lang.Object)
     */
    public void sendMessage(ID toContainerID, Object data)
            throws IOException {
        if (isInactive()) {
            return;
        } else {
            container.sendMessage(sharedObjectID,toContainerID,data); 
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class clazz) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObjectContext#getServiceAccess()
     */
    public IOSGIService getServiceAccess() {
        if (isInactive()) {
            return null;
        } else {
            return container.getServiceAccess(); 
        }
    }

}
