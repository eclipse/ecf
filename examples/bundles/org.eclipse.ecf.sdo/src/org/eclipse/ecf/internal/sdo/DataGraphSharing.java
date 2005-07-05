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

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.sdo.IDataGraphSharing;
import org.eclipse.ecf.sdo.IPublicationCallback;
import org.eclipse.ecf.sdo.ISharedDataGraph;
import org.eclipse.ecf.sdo.ISubscriptionCallback;
import org.eclipse.ecf.sdo.IUpdateConsumer;
import org.eclipse.ecf.sdo.IUpdateProvider;

import commonj.sdo.DataGraph;

/**
 * @author pnehrer
 */
public class DataGraphSharing implements
        IDataGraphSharing, ISharedObject {

    static final String DATA_GRAPH_SHARING_ID = DataGraphSharing.class
            .getName();

    private ISharedObjectConfig config;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.IDataGraphSharing#publish(commonj.sdo.DataGraph,
     *      org.eclipse.ecf.core.identity.ID,
     *      org.eclipse.ecf.sdo.IUpdateProvider,
     *      org.eclipse.ecf.sdo.IUpdateConsumer,
     *      org.eclipse.ecf.sdo.IPublicationCallback)
     */
    public synchronized ISharedDataGraph publish(DataGraph dataGraph, ID id,
            IUpdateProvider provider, IUpdateConsumer consumer,
            IPublicationCallback callback) throws ECFException {

        if (config == null)
            throw new ECFException("Not initialized.");

        // create local object
        ISharedObjectManager mgr = config.getContext().getSharedObjectManager();
        SharedDataGraph sdg = new SharedDataGraph(dataGraph, provider,
                consumer, callback, null);
        mgr.addSharedObject(id, sdg, null);
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
            IUpdateProvider provider, IUpdateConsumer consumer,
            ISubscriptionCallback callback) throws ECFException {

        if (config == null)
            throw new ECFException("Not initialized.");

        // create local object
        ISharedObjectManager mgr = config.getContext().getSharedObjectManager();
        SharedDataGraph sdg = new SharedDataGraph(null, provider, consumer,
                null, callback);
        mgr.addSharedObject(id, sdg, null);
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
        if (config != null) {
        	ISharedObjectContext context = config.getContext();
        	if (context != null) {
        		if (context.getLocalContainerID().equals(containerID)) {
        			config = null;
        		}
        	}
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class clazz) {
        return null;
    }
}