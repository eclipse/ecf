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
 * Created on Dec 20, 2004
 *  
 */
package org.eclipse.ecf.provider.generic;

import java.lang.reflect.Constructor;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConnector;
import org.eclipse.ecf.core.ISharedObjectContainerTransaction;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectAddException;
import org.eclipse.ecf.core.SharedObjectConnectException;
import org.eclipse.ecf.core.SharedObjectCreateException;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectDisconnectException;
import org.eclipse.ecf.core.events.SharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.SharedObjectManagerAddEvent;
import org.eclipse.ecf.core.events.SharedObjectManagerConnectEvent;
import org.eclipse.ecf.core.events.SharedObjectManagerCreateEvent;
import org.eclipse.ecf.core.events.SharedObjectManagerDisconnectEvent;
import org.eclipse.ecf.core.events.SharedObjectManagerRemoveEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.AbstractFactory;
import org.eclipse.ecf.core.util.QueueEnqueue;
import org.eclipse.ecf.provider.Trace;

/**
 *  
 */
public class SOManager implements ISharedObjectManager {
    static Trace debug = Trace.create("sharedobjectmanager");
    SOContainer container = null;
    Vector connectors = null;

    public SOManager(SOContainer cont) {
        super();
        this.container = cont;
        connectors = new Vector();
    }

    protected void debug(String msg) {
        if (Trace.ON && debug != null) {
            debug.msg(msg + ":" + container.getID());
        }
    }

    protected void dumpStack(String msg, Throwable e) {
        if (Trace.ON && debug != null) {
            debug.dumpStack(e, msg + ":" + container.getID());
        }
    }

    protected void addConnector(ISharedObjectConnector conn) {
        connectors.add(conn);
    }

    protected boolean removeConnector(ISharedObjectConnector conn) {
        return connectors.remove(conn);
    }

    protected List getConnectors() {
        return connectors;
    }

    protected Class[] getArgTypes(String[] argTypes, Object[] args,
            ClassLoader cl) throws ClassNotFoundException {
        return AbstractFactory.getClassesForTypes(argTypes, args, cl);
    }

    protected ISharedObject makeSharedObjectInstance(final Class newClass,
            final Class[] argTypes, final Object[] args) throws Exception {
        Object newObject = null;
        try {
            newObject = AccessController
                    .doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            Constructor aConstructor = newClass
                                    .getConstructor(argTypes);
                            aConstructor.setAccessible(true);
                            return aConstructor.newInstance(args);
                        }
                    });
        } catch (java.security.PrivilegedActionException e) {
            throw e.getException();
        }
        return verifySharedObject(newObject);
    }

    protected ISharedObject verifySharedObject(Object newSharedObject) {
        if (newSharedObject instanceof ISharedObject)
            return (ISharedObject) newSharedObject;
        else
            throw new ClassCastException("shared object "
                    + newSharedObject.toString() + " does not implement "
                    + ISharedObject.class.getName());
    }

    protected ISharedObject loadSharedObject(SharedObjectDescription sd)
            throws Exception {
        // First get classloader
        ClassLoader cl = container.getClassLoaderForSharedObject(sd);
        // Then get args array from properties
        Object[] args = container.getArgsFromProperties(sd);
        // And arg types
        String[] types = container.getArgTypesFromProperties(sd);
        Class[] argTypes = getArgTypes(types, args, cl);
        // Now load top-level class
        final Class newClass = Class.forName(sd.getClassname(), true, cl);
        return makeSharedObjectInstance(newClass, argTypes, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectManager#getSharedObjectIDs()
     */
    public ID[] getSharedObjectIDs() {
        debug("getSharedObjectIDs()");
        return container.getSharedObjectIDs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectManager#createSharedObject(org.eclipse.ecf.core.SharedObjectDescription,
     *      org.eclipse.ecf.core.ISharedObjectContainerTransaction)
     */
    public ID createSharedObject(SharedObjectDescription sd,
            ISharedObjectContainerTransaction trans)
            throws SharedObjectCreateException {
        debug("createSharedObject(" + sd + "," + trans + ")");
        // notify listeners
        if (sd == null) throw new SharedObjectCreateException("SharedObjectDescription cannot be null");
        ID sharedObjectID = sd.getID();
        if (sharedObjectID == null) throw new SharedObjectCreateException("New object ID cannot be null");
        container.fireContainerEvent(new SharedObjectManagerCreateEvent(container.getID(),sd));
        ISharedObject newObject = null;
        Throwable t = null;
        ID result = sharedObjectID;
        try {
            newObject = loadSharedObject(sd);
            result = addSharedObject(sharedObjectID, newObject, sd.getProperties(), trans);
        } catch (Exception e) {
            dumpStack("Exception in createSharedObject",e);
            SharedObjectCreateException newExcept = new SharedObjectCreateException("Container "+container.getID()+" had exception creating shared object "+sharedObjectID+": "+e.getClass().getName()+": "+e.getMessage());
            newExcept.setStackTrace(e.getStackTrace());
            throw newExcept;
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectManager#addSharedObject(org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.core.ISharedObject, java.util.Map,
     *      org.eclipse.ecf.core.ISharedObjectContainerTransaction)
     */
    public ID addSharedObject(ID sharedObjectID, ISharedObject sharedObject,
            Map properties, ISharedObjectContainerTransaction trans)
            throws SharedObjectAddException {
        debug("addSharedObject(" + sharedObjectID + "," + sharedObject + ","
                + properties + "," + trans + ")");
        // notify listeners
        container.fireContainerEvent(new SharedObjectManagerAddEvent(container.getID(),sharedObjectID,sharedObject,properties));
        Throwable t = null;
        ID result = sharedObjectID;
        try {
            ISharedObject so = sharedObject;
            SharedObjectDescription sd = new SharedObjectDescription(
                    sharedObject.getClass().getClassLoader(), sharedObjectID,
                    container.getID(), sharedObject.getClass().getName(),
                    properties, 0);
            container.addSharedObjectAndWait(sd, so, trans);
        } catch (Exception e) {
            dumpStack("Exception in addSharedObject",e);
            SharedObjectAddException newExcept = new SharedObjectAddException("Container "+container.getID()+" had exception adding shared object "+sharedObjectID+": "+e.getClass().getName()+": "+e.getMessage());
            newExcept.setStackTrace(e.getStackTrace());
            throw newExcept;
        }
        // notify listeners
        container.fireContainerEvent(new SharedObjectActivatedEvent(container.getID(), result, container.getGroupMemberIDs()));
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectManager#getSharedObject(org.eclipse.ecf.core.identity.ID)
     */
    public ISharedObject getSharedObject(ID sharedObjectID) {
        debug("getSharedObject(" + sharedObjectID + ")");
        return container.getSharedObject(sharedObjectID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectManager#removeSharedObject(org.eclipse.ecf.core.identity.ID)
     */
    public ISharedObject removeSharedObject(ID sharedObjectID) {
        debug("removeSharedObject(" + sharedObjectID + ")");
        // notify listeners
        container.fireContainerEvent(new SharedObjectManagerRemoveEvent(container.getID(),sharedObjectID));
        return container.removeSharedObject(sharedObjectID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectManager#connectSharedObjects(org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.core.identity.ID[])
     */
    public ISharedObjectConnector connectSharedObjects(ID sharedObjectFrom,
            ID[] sharedObjectsTo) throws SharedObjectConnectException {
        debug("connectSharedObjects(" + sharedObjectFrom + ","
                + sharedObjectsTo + ")");
        // notify listeners
        container.fireContainerEvent(new SharedObjectManagerConnectEvent(container.getID(),sharedObjectFrom,sharedObjectsTo));
        if (sharedObjectFrom == null)
            throw new SharedObjectConnectException("sender cannot be null");
        if (sharedObjectsTo == null)
            throw new SharedObjectConnectException("receivers cannot be null");
        ISharedObjectConnector result = null;
        synchronized (container.getGroupMembershipLock()) {
            // Get from to make sure it's there
            SOWrapper wrap = container.getSharedObjectWrapper(sharedObjectFrom);
            if (wrap == null)
                throw new SharedObjectConnectException("sender object "
                        + sharedObjectFrom.getName() + " not found");
            QueueEnqueue[] queues = new QueueEnqueue[sharedObjectsTo.length];
            for (int i = 0; i < sharedObjectsTo.length; i++) {
                SOWrapper w = container
                        .getSharedObjectWrapper(sharedObjectsTo[i]);
                if (w == null)
                    throw new SharedObjectConnectException("receiver object "
                            + sharedObjectsTo[i].getName() + " not found");
                queues[i] = new QueueEnqueueImpl(w.getQueue());
            }
            // OK now we've got ids and wrappers, make a connector
            result = new SOConnector(sharedObjectFrom, sharedObjectsTo, queues);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectManager#disconnectSharedObjects(org.eclipse.ecf.core.ISharedObjectConnector)
     */
    public void disconnectSharedObjects(ISharedObjectConnector connector)
            throws SharedObjectDisconnectException {
        if (connector != null) {
            debug("disconnectSharedObjects(" + connector.getSender() + ")");
            // notify listeners
            container.fireContainerEvent(new SharedObjectManagerDisconnectEvent(container.getID(),connector.getSender()));
        }
        if (connector == null)
            throw new SharedObjectDisconnectException("connect cannot be null");
        if (!removeConnector(connector)) {
            throw new SharedObjectDisconnectException("connector " + connector
                    + " not found");
        }
        connector.dispose();
    }

    protected void dispose() {
        debug("dispose()");
        for (Enumeration e = connectors.elements(); e.hasMoreElements();) {
            ISharedObjectConnector conn = (ISharedObjectConnector) e
                    .nextElement();
            conn.dispose();
        }
        connectors.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObjectManager#getSharedObjectConnectors(org.eclipse.ecf.core.identity.ID)
     */
    public List getSharedObjectConnectors(ID sharedObjectFrom) {
        debug("getSharedObjectConnectors(" + sharedObjectFrom + ")");
        List results = new ArrayList();
        for (Enumeration e = connectors.elements(); e.hasMoreElements();) {
            ISharedObjectConnector conn = (ISharedObjectConnector) e
                    .nextElement();
            if (sharedObjectFrom.equals(conn.getSender())) {
                results.add(conn);
            }
        }
        return results;
    }

    public static Class[] getClassesForTypes(String[] argTypes, Object[] args,
            ClassLoader cl) throws ClassNotFoundException {
        Class clazzes[] = null;
        if (args == null || args.length == 0)
            clazzes = new Class[0];
        else if (argTypes != null) {
            clazzes = new Class[argTypes.length];
            for (int i = 0; i < argTypes.length; i++) {
                clazzes[i] = Class.forName(argTypes[i], true, cl);
            }
        } else {
            clazzes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null)
                    clazzes[i] = null;
                else
                    clazzes[i] = args[i].getClass();
            }
        }
        return clazzes;
    }
}