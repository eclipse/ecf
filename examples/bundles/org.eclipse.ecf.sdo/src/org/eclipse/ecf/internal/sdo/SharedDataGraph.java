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

import java.io.IOException;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.ISharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.sdo.IPublicationCallback;
import org.eclipse.ecf.sdo.ISharedDataGraph;
import org.eclipse.ecf.sdo.ISubscriptionCallback;
import org.eclipse.ecf.sdo.IUpdateConsumer;
import org.eclipse.ecf.sdo.IUpdateProvider;
import org.eclipse.ecf.sdo.SDOPlugin;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataGraph;

/**
 * @author pnehrer
 */
public class SharedDataGraph implements ISharedObject, ISharedDataGraph {

    public static final String TRACE_TAG = "SharedDataGraph";

    private final IUpdateConsumer updateConsumer;

    private final ISubscriptionCallback subscriptionCallback;

    private final IPublicationCallback publicationCallback;

    private final IUpdateProvider updateProvider;

    private ISharedObjectConfig config;

    private DataGraph dataGraph;

    private Version version;

    SharedDataGraph(DataGraph dataGraph, IUpdateProvider updateProvider,
            IUpdateConsumer updateConsumer,
            IPublicationCallback publicationCallback,
            ISubscriptionCallback subscriptionCallback) {
        if (updateProvider == null)
            throw new IllegalArgumentException("updateProvider");

        if (updateConsumer == null)
            throw new IllegalArgumentException("updateConsumer");

        this.dataGraph = dataGraph;
        this.updateProvider = updateProvider;
        this.updateConsumer = updateConsumer;
        this.publicationCallback = publicationCallback;
        this.subscriptionCallback = subscriptionCallback;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.ISharedDataGraph#getID()
     */
    public synchronized ID getID() {
        return config == null ? null : config.getSharedObjectID();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.ISharedDataGraph#getDataGraph()
     */
    public synchronized DataGraph getDataGraph() {
        return dataGraph;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.ISharedDataGraph#dispose()
     */
    public synchronized void dispose() {
        if (config != null)
            config.getContext().getSharedObjectManager().removeSharedObject(
                    config.getSharedObjectID());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.ISharedDataGraph#commit()
     */
    public synchronized void commit() throws ECFException {
        if (config == null)
            throw new ECFException("Object is disconnected.");

        if (dataGraph == null)
            throw new ECFException("Not subscribed.");

        ChangeSummary changeSummary = dataGraph.getChangeSummary();
        if (changeSummary.getChangedDataObjects().isEmpty())
            return;

        changeSummary.endLogging();
        byte[] data = updateProvider.createUpdate(this);
        try {
            config.getContext().sendMessage(null,
                    new UpdateDataGraphMessage(version, data));
        } catch (IOException e) {
            throw new ECFException(e);
        }

        changeSummary.beginLogging();
        version = version.getNext(config.getContext().getLocalContainerID());
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

        if (version == null)
            version = new Version(config.getSharedObjectID());

        if (dataGraph != null)
            dataGraph.getChangeSummary().beginLogging();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.util.Event)
     */
    public void handleEvent(Event event) {
        if (event instanceof ISharedObjectActivatedEvent
                && ((ISharedObjectActivatedEvent) event).getActivatedID()
                        .equals(config.getSharedObjectID())) {
            synchronized (this) {
                if (dataGraph == null) {
                    try {
                        config.getContext().sendMessage(null,
                                new RequestDataGraphMessage());
                    } catch (IOException e) {
                        if (subscriptionCallback != null)
                            subscriptionCallback.subscriptionFailed(this, e);
                    }
                } else if (publicationCallback != null)
                    publicationCallback.dataGraphPublished(this);
            }
        } else if (event instanceof ISharedObjectDeactivatedEvent
                && ((ISharedObjectDeactivatedEvent) event).getDeactivatedID()
                        .equals(config.getSharedObjectID())) {
            synchronized (this) {
                if (dataGraph != null
                        && dataGraph.getChangeSummary().isLogging())
                    dataGraph.getChangeSummary().endLogging();
            }
        } else if (event instanceof ISharedObjectMessageEvent) {
            ISharedObjectMessageEvent e = (ISharedObjectMessageEvent) event;
            Object msg = e.getData();
            if (msg instanceof RequestDataGraphMessage)
                handleRequestDataGraphMessage(e.getRemoteContainerID());
            else if (msg instanceof ReceiveDataGraphMessage) {
                ReceiveDataGraphMessage m = (ReceiveDataGraphMessage) msg;
                handleReceiveDataGraphMessage(e.getRemoteContainerID(), m
                        .getVersion(), m.getData());
            } else if (msg instanceof UpdateDataGraphMessage) {
                UpdateDataGraphMessage m = (UpdateDataGraphMessage) msg;
                handleUpdateDataGraphMessage(e.getRemoteContainerID(), m
                        .getVersion(), m.getData());
            }
        }
    }

    private synchronized void handleRequestDataGraphMessage(ID containerID) {
        if (dataGraph == null)
            return;

        try {
            Object data = updateProvider.serializeDataGraph(dataGraph);
            config.getContext().sendMessage(containerID,
                    new ReceiveDataGraphMessage(version, data));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private synchronized void handleReceiveDataGraphMessage(ID containerID,
            Version version, Object data) {
        if (dataGraph == null) {
            try {
                dataGraph = updateProvider.deserializeDataGraph(data);
            } catch (IOException e) {
                // keep waiting; maybe we can successfully deserialize another
                // message...
                return;
            } catch (ClassNotFoundException e) {
                // keep waiting; maybe we can successfully deserialize another
                // message...
                return;
            }

            this.version = version;
            dataGraph.getChangeSummary().beginLogging();
            if (subscriptionCallback != null)
                subscriptionCallback.dataGraphSubscribed(this, containerID);
        }
    }

    private synchronized void handleUpdateDataGraphMessage(ID containerID,
            Version version, Object data) {
        if (dataGraph == null)
            return;

        if (!version.equals(this.version)) {
            if (SDOPlugin.isTracing(TRACE_TAG))
                SDOPlugin.getTraceLog().println(
                        "Version mismatch: current=" + this.version + "; new="
                                + version);

            updateConsumer.updateFailed(this, containerID, null);
            return;
        }

        try {
            updateProvider.applyUpdate(this, data);
        } catch (ECFException e) {
            updateConsumer.updateFailed(this, containerID, e);
            return;
        }

        if (updateConsumer.consumeUpdate(this, containerID))
            this.version = version.getNext(containerID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#handleEvents(org.eclipse.ecf.util.Event[])
     */
    public void handleEvents(Event[] events) {
        for (int i = 0; i < events.length; ++i)
            handleEvent(events[i]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.identity.ID)
     */
    public synchronized void dispose(ID containerID) {
        if (config != null) {
            // TODO Do we even have a context now?
            ISharedObjectContext context = config.getContext();
            if (context != null
                    && context.getLocalContainerID().equals(containerID)) {
                config = null;
            }
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("SharedDataGraph[");
        buf.append("provider=").append(updateProvider).append(";");
        buf.append("consumer=").append(updateConsumer).append(";");
        buf.append("callback=").append(subscriptionCallback).append(";");
        buf.append("config=").append(config).append(";");
        buf.append("dataGraph=").append(dataGraph).append(";");
        buf.append("version=").append(version).append("]");
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class clazz) {
        return null;
    }
}