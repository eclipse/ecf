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
package org.eclipse.ecf.core.sharedobject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.eclipse.ecf.core.IIdentifiable;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.core.util.IQueueEnqueue;
import org.eclipse.ecf.core.util.QueueException;
import org.eclipse.ecf.internal.core.Trace;

/**
 * Abstract base class for shared object implementation classes.
 *
 */
public abstract class AbstractSharedObject implements ISharedObject,
		IIdentifiable {
	
	private static final Trace trace = Trace.create("abstractsharedobject");
	
	private ISharedObjectConfig config = null;
	private List eventProcessors = new Vector();
	
	public AbstractSharedObject() {
		super();
	}
	public void init(ISharedObjectConfig initData)
			throws SharedObjectInitException {
		this.config = initData;
		trace("init("+initData+")");
		initialize();
	}
	/**
	 * Initialize this shared object.  Subclasses may override as appropriate.
	 *
	 */
	protected void initialize() {}
	/**
	 * Called by replication strategy code (e.g. two phase commit) to associate SharedObjectDescription with a target receiver. 
	 * This implementation returns null, indicating that no replicas will be created.  Subclasses may override as appropriate.
	 * 
	 * @param receivers an ID array of the target containers to receive replicas of this shared object
	 * @return an array of SharedObjectDescriptions.  The returned SharedObjectDescriptions must be of same 
	 * length as receivers array, or if receivers is null (meaning all in group) then of length 1.  Returning null means
	 * no replicas are to be sent.
	 */
    protected SharedObjectDescription[] getReplicaDescriptions(ID [] receivers) {
    	return null;
    }
    /**
     * Called by replication strategy code (e.g. two phase commit) when creation is completed (i.e. when transactional 
     * replication completed successfully).  Subclasses that need to be notified when creation is completed should 
     * override this method.
     *
     */
    protected void creationCompleted() {
    	trace("creationCompleted()");
    }
	public void dispose(ID containerID) {
		trace("dispose("+containerID+")");
		eventProcessors.clear();
		config = null;
	}
	public Object getAdapter(Class adapter) {
		return null;
	}
	public void handleEvent(Event event) {
		trace("handleEvent("+event+")");
		synchronized (eventProcessors) {
			fireEventProcessors(event);
		}
	}
	protected boolean addEventProcessor(IEventProcessor proc) {
		return eventProcessors.add(proc);
	}
	protected boolean removeEventProcessor(IEventProcessor proc) {
		return eventProcessors.remove(proc);
	}
	protected void clearEventProcessors() {
		eventProcessors.clear();
	}
	protected void handleUnhandledEvent(Event event) {
		trace("handleUnhandledEvent("+event+")");
	}
	protected void fireEventProcessors(Event event) {
		if (event == null) return;
		Event evt = event;
		trace("fireEventProcessors("+event+")");
		if (eventProcessors.size()==0) {
			handleUnhandledEvent(event);
			return;
		}
		for(Iterator i=eventProcessors.iterator(); i.hasNext(); ) {
			IEventProcessor ep = (IEventProcessor) i.next();
			if (ep.acceptEvent(evt)) {
				trace("calling eventProcessor="+ep+" for event="+evt);
				Event res = ep.processEvent(evt);
				trace("eventProcessor="+ep+" returned event "+res);
				if (res == null) {
					trace("discontinuing processing of event "+evt);
					return;
				}
			} else {
				trace("eventProcessor="+ep+" refused event="+evt);
			}
		}
	}
	public void handleEvents(Event[] events) {
		trace("handleEvents("+Arrays.asList(events)+")");
		if (events == null) return;
		for(int i=0; i < events.length; i++) {
			handleEvent(events[i]);
		}
	}
	public ID getID() {
		return getConfig().getSharedObjectID();
	}
	protected ISharedObjectConfig getConfig() {
		return config;
	}
	protected ISharedObjectContext getContext() {
		return getConfig().getContext();
	}
	protected ID getPrimaryContainerID() {
		return getConfig().getHomeContainerID();
	}
	protected ID getLocalID() {
		return getContext().getLocalContainerID();
	}
	protected ID getGroupID() {
		return getContext().getConnectedID();
	}
	protected boolean isPrimary() {
		ID local = getLocalID();
		ID home = getPrimaryContainerID();
		if (local == null || home == null) {
			return false;
		} else return (local.equals(home));
	}
	protected Map getProperties() {
		return getConfig().getProperties();
	}
    protected void destroySelf() {
    	trace("destroySelf()");
        if (isPrimary()) {
            try {
                // Send destroy message to all known remotes
                destroyRemote(null);
            } catch (IOException e) {
                traceStack("Exception sending destroy message to remotes", e);
            }
        }
        destroySelfLocal();
    }
    protected void destroySelfLocal() {
    	trace("destroySelfLocal()");
        try {
            ISharedObjectManager manager = getContext().getSharedObjectManager();
            if (manager != null) {
                manager.removeSharedObject(getID());
            }
        } catch (Exception e) {
            traceStack("Exception in destroySelfLocal()",e);
        }
    }
    protected void destroyRemote(ID remoteID) throws IOException {
        trace("destroyRemote("+remoteID+")");
    	getContext().sendDispose(remoteID);
    }
	private void trace(String msg) {
		if (Trace.ON && trace != null) {
			trace.msg(getID()+":"+msg);
		}
	}
	private void traceStack(String msg, Throwable t) {
		if (Trace.ON && trace != null) {
			trace.dumpStack(t,getID()+":"+msg);
		}
	}
    protected void sendSharedObjectMsgTo(ID toID, SharedObjectMsg msg)
			throws IOException {
		getContext().sendMessage(toID,
				new SharedObjectMsgEvent(getID(), toID, msg));
	}

    protected void sendSharedObjectMsgToPrimary(SharedObjectMsg msg) throws IOException {
    	sendSharedObjectMsgTo(getPrimaryContainerID(), msg);
    }
    protected void sendSharedObjectMsgToSelf(SharedObjectMsg msg) {
		ISharedObjectContext context = getContext();
		if (context == null)
			return;
		IQueueEnqueue queue = context.getQueue();
		try {
			queue.enqueue(new SharedObjectMsgEvent(getID(), getContext()
					.getLocalContainerID(), msg));
		} catch (QueueException e) {
			traceStack("QueueException enqueing message to self", e);
			return;
		}
	}

}
