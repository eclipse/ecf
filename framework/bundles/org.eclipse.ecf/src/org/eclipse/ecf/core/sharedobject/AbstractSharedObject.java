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
import org.eclipse.ecf.core.ReplicaSharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.core.util.IQueueEnqueue;
import org.eclipse.ecf.core.util.QueueException;
import org.eclipse.ecf.internal.core.Trace;

/**
 * Base class for shared object classes.
 *
 */
public class AbstractSharedObject implements ISharedObject,
		IIdentifiable {
	
	private static final Trace trace = Trace.create("abstractsharedobject");
	
	private ISharedObjectConfig config = null;
	private List eventProcessors = new Vector();
	
	public AbstractSharedObject() {
		super();
	}
	public final void init(ISharedObjectConfig initData)
			throws SharedObjectInitException {
		this.config = initData;
		trace("init("+initData+")");
		addEventProcessor(new SharedObjectMsgEventProcessor(this));
		initialize();
	}
	/**
	 * Initialize this shared object.  This initializes the object by adding a
	 * single SharedObjectMsgEventProcessors to the set of event processors
	 * owned by this shared object.  This SharedObjectMsgEventProcessor will
	 * call the {@link #handleSharedObjectMsgEvent(SharedObjectMsgEvent) }
	 * method to process received SharedObjectMsgEvent.
	 * Subclasses may override as appropriate.  If they wish to retain the
	 * SharedObjectMessageEventProcessor behavior described above, however,
	 * they should call this method;
	 *
	 * @throws SharedObjectInitException if initialization should throw
	 */
	protected void initialize() throws SharedObjectInitException {}
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
	protected ID getHomeContainerID() {
		return getConfig().getHomeContainerID();
	}
	protected ID getLocalContainerID() {
		return getContext().getLocalContainerID();
	}
	protected ID getGroupID() {
		return getContext().getConnectedID();
	}
	protected boolean isPrimary() {
		ID local = getLocalContainerID();
		ID home = getHomeContainerID();
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
	/**
	 * Send SharedObjectMessage to container with given ID.  The toID 
	 * parameter may be null, and if null the message will be delivered to 
	 * <b>all</b> containers in group.  The second parameter may not be null.
	 * 
	 * @param toID the target container ID for the SharedObjectMsg.  If null, the 
	 * given message is sent to all other containers currently in group
	 * @param msg the message instance to send
	 * @throws IOException thrown if the local container is not connected or unable
	 * to send for other reason
	 */
    protected void sendSharedObjectMsgTo(ID toID, SharedObjectMsg msg)
			throws IOException {
    	if (msg == null) throw new NullPointerException("msg cannot be null");
		getContext().sendMessage(toID,
				new SharedObjectMsgEvent(getID(), toID, msg));
	}
    /**
     * Send SharedObjectMsg to this shared object's primary instance.
     * @param msg the message instance to send
     * @throws IOException throws if the local container is not connect or unable 
     * to send for other reason
     */
    protected void sendSharedObjectMsgToPrimary(SharedObjectMsg msg) throws IOException {
    	sendSharedObjectMsgTo(getHomeContainerID(), msg);
    }
    /**
     * Send SharedObjectMsg to local shared object.  This places the given message at
     * the end of this shared object's message queue for processing.
     * @param msg the message instance to send.
     */
    protected void sendSharedObjectMsgToSelf(SharedObjectMsg msg) {
    	if (msg == null) throw new NullPointerException("msg cannot be null");
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
    /**
     * Handle a SharedObjectMessageEvent.  This method will be automatically called by 
     * the SharedObjectMsgEventProcessor when a SharedObjectMessageEvent is received.
     * The SharedObjectMsgEventProcessor is associated with this object via the initialize()
     * method
     * @param event the event to handle
     * @return Event the Event for subsequent processing.  If null, the provided event
     * will receive no further processing.  If non-null the provided Event will be 
     * passed to subsequent event processors.
     */
    protected Event handleSharedObjectMsgEvent(SharedObjectMsgEvent event) {
    	trace("handleSharedObjectMsgEvent("+event+")");
    	return event;
    }
	/**
	 * Get a ReplicaSharedObjectDescription for a replica to be created on a given receiver.
	 * 
	 * @param receiver the receiver the ReplicaSharedObjectDescription is for
	 * @return ReplicaSharedObjectDescription to be associated with given receiver.  A non-null
	 * ReplicaSharedObjectDescription <b>must</b> be returned.
	 */
	protected ReplicaSharedObjectDescription getReplicaDescription(ID receiver) {
		return new ReplicaSharedObjectDescription(getClass(),getID(),getConfig().getHomeContainerID(),
	    		getConfig().getProperties());
	}
	/**
	 * This method is called by replicateToRemoteContainers to
	 * determine the ReplicaSharedObjectDescriptions associated with the given receivers.  Receivers
	 * may be null (meaning that all in group are to be receivers), and if so then this method
	 * should return a ReplicaSharedObjectDescription [] of length 1 with a single ReplicaSharedObjectDescription
	 * that will be used for all receivers.  If receivers is non-null, then the ReplicaSharedObjectDescription [] 
	 * result must be of <b>same length</b> as the receivers array.  This method calls the
	 * getReplicaDescription method to create a replica description for each receiver.  If this method returns
	 * null, <b>null replication is done</b>.
	 * 
	 * @param receivers an ID[] of the intended receivers for the resulting ReplicaSharedObjectDescriptions.  If null,
	 * then the <b>entire current group</b> is assumed to be the target, and this method should return a
	 * ReplicaSharedObjectDescriptions array of length 1, with a single ReplicaSharedObjectDescriptions for all target receivers.
	 * 
	 * @return ReplicaSharedObjectDescription[] to determine replica descriptions for each receiver.  A null return
	 * value indicates that no replicas are to be created.  If the returned array is not null, then it <b>must</b>
	 * be of same length as the receivers parameter.
	 * 
	 */
	protected ReplicaSharedObjectDescription[] getReplicaDescriptions(ID[] receivers) {
		ReplicaSharedObjectDescription[] descriptions = null;
		if (receivers == null || receivers.length == 1) {
			descriptions = new ReplicaSharedObjectDescription[1];
			descriptions[0] = getReplicaDescription((receivers==null)?null:receivers[0]);
		} else {
			descriptions = new ReplicaSharedObjectDescription[receivers.length];
			for(int i=0; i < receivers.length; i++) {
				descriptions[i] = getReplicaDescription(receivers[i]);
			}
		}
		return descriptions;
	}
	/**
	 * Replicate this shared object to a given set of remote containers. This
	 * method will invoke the method getReplicaDescriptions in order to
	 * determine the set of ReplicaSharedObjectDescriptions to send to remote
	 * containers.
	 * 
	 * @param remoteContainers
	 *            the set of remote containers to replicate to. If null, <b>all</b>
	 *            containers in the current group are sent a message to create a
	 *            replica of this shared object.
	 */
	protected void replicateToRemoteContainers(ID[] remoteContainers) {
		if (remoteContainers == null)
			trace("replicateTo(null)");
		else
			trace("replicateTo(" + Arrays.asList(remoteContainers) + ")");
		try {
			// Get current group membership
			ISharedObjectContext context = getContext();
			if (context == null)
				return;
			ID[] group = context.getGroupMemberIDs();
			if (group == null || group.length < 1) {
				// we're done
				return;
			}
			ReplicaSharedObjectDescription[] createInfos = getReplicaDescriptions(remoteContainers);
			if (createInfos != null) {
				if (createInfos.length == 1) {
					context.sendCreate((remoteContainers == null) ? null
							: remoteContainers[0], createInfos[0]);
				} else {
					for (int i = 0; i < remoteContainers.length; i++) {
						context.sendCreate(remoteContainers[i], createInfos[i]);
					}
				}
			}
		} catch (IOException e) {
			if (remoteContainers == null)
				traceStack("Exception in replicateTo(null)", e);
			else
				traceStack("Exception in replicateTo("
						+ Arrays.asList(remoteContainers) + ")", e);
			return;
		}
	}

}
