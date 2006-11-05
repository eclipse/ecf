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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IIdentifiable;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectCreateResponseEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.sharedobject.events.RemoteSharedObjectEvent;
import org.eclipse.ecf.core.sharedobject.util.IQueueEnqueue;
import org.eclipse.ecf.core.sharedobject.util.QueueException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.core.sharedobject.Activator;
import org.eclipse.ecf.internal.core.sharedobject.SharedObjectDebugOptions;

/**
 * Base class for shared object classes.
 * 
 */
public class BaseSharedObject implements ISharedObject, IIdentifiable {

	private static final int DESTROYREMOTE_CODE = 8001;

	private ISharedObjectConfig config = null;

	private List eventProcessors = new Vector();

	public BaseSharedObject() {
		super();
	}

	private void traceEntering(String methodName, Object[] params) {
		Trace.entering(Activator.getDefault(),
				SharedObjectDebugOptions.METHODS_ENTERING,
				BaseSharedObject.class, getID() + "." + methodName, params);
	}

	private void traceEntering(String methodName, Object param) {
		Trace.entering(Activator.getDefault(),
				SharedObjectDebugOptions.METHODS_ENTERING,
				BaseSharedObject.class, getID() + "." + methodName, param);
	}

	private void traceExiting(String methodName, Object result) {
		Trace.entering(Activator.getDefault(),
				SharedObjectDebugOptions.METHODS_EXITING,
				BaseSharedObject.class, getID() + "." + methodName, result);
	}

	public final void init(ISharedObjectConfig initData)
			throws SharedObjectInitException {
		String methodName = "init";
		traceEntering(methodName, new Object[] { initData });
		this.config = initData;
		addEventProcessor(new SharedObjectMsgEventProcessor(this));
		initialize();
		traceExiting(methodName, null);
	}

	/**
	 * Initialize this shared object. Subclasses may override as appropriate to
	 * define custom initialization behavior. If initialization should fail,
	 * then a SharedObjectInitException should be thrown by implementing code.
	 * Also, subclasses overriding this method should call super.initialize()
	 * before running their own code.
	 * 
	 * @throws SharedObjectInitException
	 *             if initialization should throw
	 */
	protected void initialize() throws SharedObjectInitException {
		traceEntering("initialize", null);
	}

	/**
	 * Called by replication strategy code (e.g. two phase commit) when creation
	 * is completed (i.e. when transactional replication completed
	 * successfully). Subclasses that need to be notified when creation is
	 * completed should override this method.
	 * 
	 */
	protected void creationCompleted() {
		traceEntering("creationCompleted", null);
	}

	public void dispose(ID containerID) {
		traceEntering("dispose", new Object[] { containerID });
		eventProcessors.clear();
		config = null;
		traceExiting("dispose", null);
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public void handleEvent(Event event) {
		traceEntering("handleEvent", event);
		Trace.trace(Activator.getDefault(), getID() + ":" + "handleEvent("
				+ event + ")");
		synchronized (eventProcessors) {
			fireEventProcessors(event);
		}
		traceExiting("handleEvent", event);
	}

	public boolean addEventProcessor(IEventProcessor proc) {
		return eventProcessors.add(proc);
	}

	public boolean removeEventProcessor(IEventProcessor proc) {
		return eventProcessors.remove(proc);
	}

	public void clearEventProcessors() {
		eventProcessors.clear();
	}

	protected void handleUnhandledEvent(Event event) {
		traceEntering("handleUnhandledEvent", event);
	}

	protected void fireEventProcessors(Event event) {
		if (event == null)
			return;
		Event evt = event;
		if (eventProcessors.size() == 0) {
			handleUnhandledEvent(event);
			return;
		}
		for (Iterator i = eventProcessors.iterator(); i.hasNext();) {
			IEventProcessor ep = (IEventProcessor) i.next();
			if (ep.processEvent(evt))
				break;
		}
	}

	public void handleEvents(Event[] events) {
		traceEntering("handleEvents", events);
		if (events == null)
			return;
		for (int i = 0; i < events.length; i++) {
			handleEvent(events[i]);
		}
		traceExiting("handleEvents", null);
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

	protected boolean isConnected() {
		return (getContext().getConnectedID() != null);
	}

	protected boolean isPrimary() {
		ID local = getLocalContainerID();
		ID home = getHomeContainerID();
		if (local == null || home == null) {
			return false;
		} else
			return (local.equals(home));
	}

	protected Map getProperties() {
		return getConfig().getProperties();
	}

	protected void destroySelf() {
		traceEntering("destroySelf", null);
		if (isPrimary()) {
			try {
				// Send destroy message to all known remotes
				destroyRemote(null);
			} catch (IOException e) {
				traceCatching("destroySelfLocal", e);
				Activator.getDefault().getLog()
						.log(
								new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										DESTROYREMOTE_CODE,
										"Exception in destroyRemote for "
												+ getID(), e));
			}
		}
		destroySelfLocal();
		traceExiting("destroySelf", null);
	}

	protected void destroySelfLocal() {
		traceEntering("destroySelfLocal", null);
		try {
			ISharedObjectManager manager = getContext()
					.getSharedObjectManager();
			if (manager != null) {
				manager.removeSharedObject(getID());
			}
		} catch (Exception e) {
			traceCatching("destroySelfLocal", e);
		}
		traceExiting("destroySelfLocal", null);
	}

	protected void destroyRemote(ID remoteID) throws IOException {
		getContext().sendDispose(remoteID);
	}

	private void traceCatching(String method, Throwable t) {
		Trace.catching(Activator.getDefault(),
				SharedObjectDebugOptions.EXCEPTIONS_CATCHING,
				BaseSharedObject.class, getID() + "." + method, t);
	}

	/**
	 * Send SharedObjectMessage to container with given ID. The toID parameter
	 * may be null, and if null the message will be delivered to <b>all</b>
	 * containers in group. The second parameter may not be null.
	 * 
	 * @param toID
	 *            the target container ID for the SharedObjectMsg. If null, the
	 *            given message is sent to all other containers currently in
	 *            group
	 * @param msg
	 *            the message instance to send
	 * @throws IOException
	 *             thrown if the local container is not connected or unable to
	 *             send for other reason
	 */
	protected void sendSharedObjectMsgTo(ID toID, SharedObjectMsg msg)
			throws IOException {
		if (msg == null)
			throw new NullPointerException("msg cannot be null");
		String method = "sendSharedObjectMsgTo";
		traceEntering(method, new Object[] { toID, msg });
		getContext().sendMessage(toID,
				new SharedObjectMsgEvent(getID(), toID, msg));
		traceExiting(method, null);
	}

	/**
	 * Send SharedObjectMsg to this shared object's primary instance.
	 * 
	 * @param msg
	 *            the message instance to send
	 * @throws IOException
	 *             throws if the local container is not connect or unable to
	 *             send for other reason
	 */
	protected void sendSharedObjectMsgToPrimary(SharedObjectMsg msg)
			throws IOException {
		sendSharedObjectMsgTo(getHomeContainerID(), msg);
	}

	/**
	 * Send SharedObjectMsg to local shared object. This places the given
	 * message at the end of this shared object's message queue for processing.
	 * 
	 * @param msg
	 *            the message instance to send.
	 */
	protected void sendSharedObjectMsgToSelf(SharedObjectMsg msg) {
		if (msg == null)
			throw new NullPointerException("msg cannot be null");
		ISharedObjectContext context = getContext();
		if (context == null)
			return;
		IQueueEnqueue queue = context.getQueue();
		try {
			queue.enqueue(new SharedObjectMsgEvent(getID(), getContext()
					.getLocalContainerID(), msg));
		} catch (QueueException e) {
			traceCatching("sendSharedObjectMsgToSelf", e);
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							DESTROYREMOTE_CODE,
							"Exception in queue.enqueue for " + getID(), e));
		}
	}

	/**
	 * Get SharedObjectMsg from ISharedObjectMessageEvent.
	 * ISharedObjectMessageEvents can come from both local and remote sources.
	 * In the remote case, the SharedObjectMsg has to be retrieved from the
	 * RemoteSharedObjectEvent rather than the
	 * ISharedObjectMessageEvent.getData() directly. This method will provide a
	 * non-null SharedObjectMsg if it's provided either via remotely or locally.
	 * Returns null if the given event does not provide a valid SharedObjectMsg.
	 * 
	 * @param event
	 * @return SharedObjectMsg the SharedObjectMsg delivered by the given event
	 */
	protected SharedObjectMsg getSharedObjectMsgFromEvent(
			ISharedObjectMessageEvent event) {
		String method = "getSharedObjectMsgFromEvent";
		traceEntering(method, event);
		Object eventData = event.getData();
		Object msgData = null;
		// If eventData is not null and instanceof RemoteSharedObjectEvent
		// then its a remote event and we extract the SharedObjectMsgEvent it
		// contains and get it's data
		if (eventData != null && eventData instanceof RemoteSharedObjectEvent) {
			// It's a remote event
			Object rsoeData = ((RemoteSharedObjectEvent) event).getData();
			if (rsoeData != null && rsoeData instanceof SharedObjectMsgEvent)
				msgData = ((SharedObjectMsgEvent) rsoeData).getData();
		} else
			msgData = eventData;
		if (msgData != null && msgData instanceof SharedObjectMsg)
			return (SharedObjectMsg) msgData;
		return null;
	}

	/**
	 * Handle a ISharedObjectMessageEvent. This method will be automatically
	 * called by the SharedObjectMsgEventProcessor when a
	 * ISharedObjectMessageEvent is received. The SharedObjectMsgEventProcessor
	 * is associated with this object via the initialize() method
	 * 
	 * @param event
	 *            the event to handle
	 * @return true if the provided event should receive no further processing.
	 *         If false the provided Event should be passed to subsequent event
	 *         processors.
	 */
	protected boolean handleSharedObjectMsgEvent(ISharedObjectMessageEvent event) {
		traceEntering("handleSharedObjectMsgEvent", event);
		if (event instanceof ISharedObjectCreateResponseEvent)
			return handleSharedObjectCreateResponseEvent((ISharedObjectCreateResponseEvent) event);
		else {
			SharedObjectMsg msg = getSharedObjectMsgFromEvent(event);
			if (msg != null)
				return handleSharedObjectMsg(msg);
			else
				return false;
		}
	}

	/**
	 * Handle a ISharedObjectCreateResponseEvent. This handler is called by
	 * handleSharedObjectMsgEvent when the ISharedObjectMessageEvent is of type
	 * ISharedObjectCreateResponseEvent. This default implementation simply
	 * returns false. Subclasses may override as appropriate. Note that if
	 * return value is true, it will prevent subsequent event processors from
	 * having a chance to process event
	 * 
	 * @param createResponseEvent
	 *            the ISharedObjectCreateResponseEvent received
	 * @return true if the provided event should receive no further processing.
	 *         If false the provided Event should be passed to subsequent event
	 *         processors.
	 */
	protected boolean handleSharedObjectCreateResponseEvent(
			ISharedObjectCreateResponseEvent createResponseEvent) {
		traceEntering("handleSharedObjectCreateResponseEvent",
				createResponseEvent);
		return false;
	}

	/**
	 * SharedObjectMsg handler method. This method will be called by
	 * {@link #handleSharedObjectMsgEvent(ISharedObjectMessageEvent)} when a
	 * SharedObjectMsg is received either from a local source or a remote
	 * source. This default implementation simply returns false so that other
	 * processing of of the given msg can occur. Subclasses should override this
	 * behavior to define custom logic for handling SharedObjectMsgs.
	 * 
	 * @param msg
	 *            the SharedObjectMsg received
	 * @return true if the msg has been completely handled and subsequent
	 *         processing should stop. False if processing should continue
	 */
	protected boolean handleSharedObjectMsg(SharedObjectMsg msg) {
		traceEntering("handleSharedObjectMsg", msg);
		return false;
	}

	/**
	 * Get a ReplicaSharedObjectDescription for a replica to be created on a
	 * given receiver.
	 * 
	 * @param receiver
	 *            the receiver the ReplicaSharedObjectDescription is for
	 * @return ReplicaSharedObjectDescription to be associated with given
	 *         receiver. A non-null ReplicaSharedObjectDescription <b>must</b>
	 *         be returned.
	 */
	protected ReplicaSharedObjectDescription getReplicaDescription(ID receiver) {
		traceEntering("getReplicaDescription", receiver);
		ReplicaSharedObjectDescription result = new ReplicaSharedObjectDescription(
				getClass(), getID(), getConfig().getHomeContainerID(),
				getConfig().getProperties());
		traceExiting("getReplicaDescription", result);
		return result;
	}

	/**
	 * This method is called by replicateToRemoteContainers to determine the
	 * ReplicaSharedObjectDescriptions associated with the given receivers.
	 * Receivers may be null (meaning that all in group are to be receivers),
	 * and if so then this method should return a ReplicaSharedObjectDescription []
	 * of length 1 with a single ReplicaSharedObjectDescription that will be
	 * used for all receivers. If receivers is non-null, then the
	 * ReplicaSharedObjectDescription [] result must be of <b>same length</b>
	 * as the receivers array. This method calls the getReplicaDescription
	 * method to create a replica description for each receiver. If this method
	 * returns null, <b>null replication is done</b>.
	 * 
	 * @param receivers
	 *            an ID[] of the intended receivers for the resulting
	 *            ReplicaSharedObjectDescriptions. If null, then the <b>entire
	 *            current group</b> is assumed to be the target, and this
	 *            method should return a ReplicaSharedObjectDescriptions array
	 *            of length 1, with a single ReplicaSharedObjectDescriptions for
	 *            all target receivers.
	 * 
	 * @return ReplicaSharedObjectDescription[] to determine replica
	 *         descriptions for each receiver. A null return value indicates
	 *         that no replicas are to be created. If the returned array is not
	 *         null, then it <b>must</b> be of same length as the receivers
	 *         parameter.
	 * 
	 */
	protected ReplicaSharedObjectDescription[] getReplicaDescriptions(
			ID[] receivers) {
		traceEntering("getReplicaDescriptions", receivers);
		ReplicaSharedObjectDescription[] descriptions = null;
		if (receivers == null || receivers.length == 1) {
			descriptions = new ReplicaSharedObjectDescription[1];
			descriptions[0] = getReplicaDescription((receivers == null) ? null
					: receivers[0]);
		} else {
			descriptions = new ReplicaSharedObjectDescription[receivers.length];
			for (int i = 0; i < receivers.length; i++) {
				descriptions[i] = getReplicaDescription(receivers[i]);
			}
		}
		traceExiting("getReplicaDescriptions", descriptions);
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
		traceEntering("replicatToRemoteContainers", remoteContainers);
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
			traceCatching("replicateToRemoteContainers", e);
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							DESTROYREMOTE_CODE,
							"Exception in replicateToRemoteContainers for "
									+ getID(), e));
		}
	}

}
