/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.sdo;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectAddException;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.sdo.IDataGraphSharing;
import org.eclipse.ecf.sdo.ISharedDataGraph;
import org.eclipse.ecf.sdo.ISubscriptionCallback;
import org.eclipse.ecf.sdo.IUpdateConsumer;
import org.eclipse.ecf.sdo.IUpdateProvider;
import org.eclipse.ecf.sdo.SDOPlugin;

import commonj.sdo.DataGraph;

/**
 * @author pnehrer
 */
public class DataGraphSharing extends PlatformObject implements
        IDataGraphSharing, ISharedObject {

    public static final String DATA_GRAPH_SHARING_ID = DataGraphSharing.class
            .getName();

    private ISharedObjectConfig config;

    private boolean debug;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.IDataGraphSharing#publish(commonj.sdo.DataGraph,
     *      org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.sdo.IUpdateProvider,
     *      org.eclipse.ecf.sdo.IUpdateConsumer)
     */
    public synchronized ISharedDataGraph publish(DataGraph dataGraph, ID id,
            IUpdateProvider provider, IUpdateConsumer consumer)
            throws CoreException {

        if (config == null)
            throw new CoreException(new Status(Status.ERROR, SDOPlugin
                    .getDefault().getBundle().getSymbolicName(), 0,
                    "Not initialized.", null));

        // create local object
        ISharedObjectManager mgr = config.getContext().getSharedObjectManager();
        SharedDataGraph sdg = new SharedDataGraph(dataGraph, provider, consumer);
        sdg.setDebug(debug);

        try {
            mgr.addSharedObject(id, sdg, null, null);
        } catch (SharedObjectAddException e) {
            throw new CoreException(new Status(Status.ERROR, SDOPlugin
                    .getDefault().getBundle().getSymbolicName(), 0,
                    "Could not publish data graph.", e));
        }

        return sdg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.IDataGraphSharing#subscribe(org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.sdo.ISubscriptionCallback,
     *      org.eclipse.ecf.sdo.IUpdateProvider,
     *      org.eclipse.ecf.sdo.IUpdateConsumer)
     */
    public synchronized ISharedDataGraph subscribe(ID id,
            ISubscriptionCallback callback, IUpdateProvider provider,
            IUpdateConsumer consumer) throws CoreException {

        if (config == null)
            throw new CoreException(new Status(Status.ERROR, SDOPlugin
                    .getDefault().getBundle().getSymbolicName(), 0,
                    "Not initialized.", null));

        // create local object
        ISharedObjectManager mgr = config.getContext().getSharedObjectManager();
        SharedDataGraph sdg = new SharedDataGraph(null, provider, consumer,
                callback);
        sdg.setDebug(debug);

        try {
            mgr.addSharedObject(id, sdg, null, null);
        } catch (SharedObjectAddException e) {
            throw new CoreException(new Status(Status.ERROR, SDOPlugin
                    .getDefault().getBundle().getSymbolicName(), 0,
                    "Could not subscribe to data graph with id" + id + ".", e));
        }

        return sdg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.IDataGraphSharing#dispose()
     */
    public synchronized void dispose() {
        if (config != null)
            config.getContext().getSharedObjectManager().removeSharedObject(
                    config.getSharedObjectID());
    }

    /**
     * Sets the debug flag.
     * 
     * @param debug
     * @deprecated Use Eclipse's plugin tracing support instead.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
     */
    public synchronized void init(ISharedObjectConfig initData)
            throws SharedObjectInitException {

        if (config == null)
            config = initData;
        else
            throw new SharedObjectInitException("Already initialized.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
     */
    public void handleEvent(Event event) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#handleEvents(org.eclipse.ecf.core.util.Event[])
     */
    public void handleEvents(Event[] events) {
        for (int i = 0; i < events.length; ++i)
            handleEvent(events[i]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
     */
    public synchronized void dispose(ID containerID) {
        if (config != null
                && config.getContext().getLocalContainerID()
                        .equals(containerID))
            config = null;
    }
}