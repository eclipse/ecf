/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare.multicast;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.ISharedObjectContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ISharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.internal.datashare.DataSharePlugin;

/**
 * @author pnehrer
 */
public abstract class AbstractMulticaster implements ISharedObject {

	public static final short NEW = 0;

	public static final short READY = 1;

	public static final short DISPOSED = 2;

	public class Testable {

		public Version getVersion() {
			return version;
		}

		public short getState() {
			return state;
		}

		public String getStateCode() {
			return getStateStr();
		}
	}

	protected final HashSet listeners = new HashSet();

	protected ISharedObjectConfig config;

	protected ID sharedObjectID;

	protected ISharedObjectContext context;

	protected ID localContainerID;

	protected ID groupID;

	protected Version version;

	protected short state = NEW;

	protected final HashSet pauses = new HashSet();

	protected HashSet pauseRequests;

	protected final HashSet groupMembers = new HashSet();

	protected Testable testable;

	public void addMessageListener(IMessageListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	public void removeMessageListener(IMessageListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}

	public abstract boolean sendMessage(Object message) throws ECFException;

	public synchronized void pause() throws ECFException, IllegalStateException {
		if (pauses.contains(localContainerID))
			throw new IllegalStateException();

		while (state == NEW) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new ECFException(e);
			}
		}

		if (state == DISPOSED)
			throw new IllegalStateException();

		boolean wasEmpty = pauses.isEmpty();
		pauses.add(localContainerID);
		pauseRequests = new HashSet(groupMembers);
		try {
			if (!pauseRequests.isEmpty()) {
				context.sendMessage(null, new Pause());
				synchronized (pauses) {
					pauses.wait(1000);
				}
			}

			if (!pauseRequests.isEmpty())
				throw new ECFException("Failed to pause.");
		} catch (IOException e) {
			pauses.remove(localContainerID);
			throw new ECFException(e);
		} catch (InterruptedException e) {
			pauses.remove(localContainerID);
			throw new ECFException(e);
		} finally {
			if (wasEmpty && !pauses.isEmpty())
				notify();
		}
	}

	public synchronized void resume() throws ECFException {
		if (state == DISPOSED)
			throw new IllegalStateException();

		if (!pauses.contains(localContainerID))
			throw new IllegalStateException();

		try {
			context.sendMessage(null, new Resume());
			pauses.remove(localContainerID);
		} catch (IOException e) {
			throw new ECFException(e);
		} finally {
			if (pauses.isEmpty())
				notify();
		}
	}

	public synchronized SharedObjectDescription createDescription() {
		HashMap params = new HashMap(1);
		params.put("version", version);
		return new SharedObjectDescription(sharedObjectID, getClass(), params);
	}

	protected void receiveMessage(Version version, Object message) {
		synchronized (listeners) {
			for (Iterator i = listeners.iterator(); i.hasNext();)
				((IMessageListener) i.next()).messageReceived(version, message);
		}
	}

	protected synchronized boolean waitToSend() {
		while (state != READY || !pauses.isEmpty()) {
			if (state == DISPOSED)
				return false;

			try {
				wait();
			} catch (InterruptedException e) {
				DataSharePlugin.log(e);
				return false;
			}
		}

		return true;
	}

	protected void traceEntry(String method) {
		StringBuffer buf = new StringBuffer("> ");
		buf.append(getStateStr());
		buf.append(' ');
		buf.append(localContainerID);
		buf.append(": ");
		buf.append(method);
		DataSharePlugin.getTraceLog().println(buf);
	}

	protected void traceExit(String method) {
		StringBuffer buf = new StringBuffer("< ");
		buf.append(getStateStr());
		buf.append(' ');
		buf.append(localContainerID);
		buf.append(": ");
		buf.append(method);
		DataSharePlugin.getTraceLog().println(buf);
	}

	protected String getStateStr() {
		switch (state) {
		case NEW:
			return "NEW";
		case READY:
			return "RDY";
		case DISPOSED:
			return "DIS";
		default:
			return "UNK";
		}
	}

	public synchronized Testable getTestable() {
		if (testable == null)
			testable = new Testable();

		return testable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
	 */
	public synchronized void init(ISharedObjectConfig config)
			throws SharedObjectInitException {
		this.config = config;
		sharedObjectID = config.getSharedObjectID();
		Map params = config.getProperties();
		if (params != null) {
			Object param = params.get("version");
			if (param != null)
				version = (Version) param;
		}

		if (version == null)
			version = new Version(sharedObjectID, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		if (event instanceof ISharedObjectActivatedEvent)
			handleActivated((ISharedObjectActivatedEvent) event);
		else if (event instanceof ISharedObjectDeactivatedEvent)
			handleDeactivated((ISharedObjectDeactivatedEvent) event);
		else if (event instanceof ISharedObjectContainerDisconnectedEvent)
			handleDeparted((ISharedObjectContainerDisconnectedEvent) event);
		else if (event instanceof ISharedObjectMessageEvent) {
			ISharedObjectMessageEvent e = (ISharedObjectMessageEvent) event;
			if (e.getData() instanceof Message)
				handleMessage(e.getRemoteContainerID(), (Message) e.getData());
			else if (e.getData() instanceof Pause)
				handlePause(e.getRemoteContainerID(), (Pause) e.getData());
			else if (e.getData() instanceof Paused)
				handlePaused(e.getRemoteContainerID(), (Paused) e.getData());
			else if (e.getData() instanceof Resume)
				handleResume(e.getRemoteContainerID(), (Resume) e.getData());
		}
	}

	protected void handleActivated(ISharedObjectActivatedEvent event) {
		if (event.getActivatedID().equals(sharedObjectID)) {
			context = config.getContext();
			localContainerID = context.getLocalContainerID();
			groupID = context.getConnectedID();
			if (groupID == null)
				try {
					context.sendDispose(localContainerID);
				} catch (IOException e) {
					DataSharePlugin.log(e);
				}
			else {
				synchronized (this) {
					groupMembers.addAll(Arrays.asList(context
							.getGroupMemberIDs()));
					groupMembers.remove(localContainerID);
					try {
						context.sendMessage(null, new Activated());
					} catch (IOException e) {
						DataSharePlugin.log(e);
					}

					state = READY;
					notifyAll();
				}
			}
		}
	}

	protected void handleDeactivated(ISharedObjectDeactivatedEvent event) {
		if (event.getDeactivatedID().equals(sharedObjectID)) {
			synchronized (this) {
				state = DISPOSED;
				notifyAll();
			}
		}
	}

	protected void handleDeparted(ISharedObjectContainerDisconnectedEvent event) {
		if (event.getTargetID().equals(localContainerID)) {
			ISharedObjectManager manager = context.getSharedObjectManager();
			if (manager != null) {
				manager.removeSharedObject(sharedObjectID);
			}
		} else {
			synchronized (this) {
				groupMembers.remove(event.getTargetID());
			}
		}
	}

	protected synchronized void handleActivated(ID remoteContainerID,
			Activated activated) {
		groupMembers.add(remoteContainerID);
	}

	protected synchronized void handlePause(ID remoteContainerID, Pause pause) {
		if (pauses.isEmpty())
			notify();

		pauses.add(remoteContainerID);
	}

	protected synchronized void handlePaused(ID remoteContainerID, Paused paused) {
		if (pauses.contains(localContainerID) && pauseRequests != null
				&& pauseRequests.remove(remoteContainerID)
				&& pauseRequests.isEmpty())
			synchronized (pauses) {
				pauses.notify();
			}
	}

	protected synchronized void handleResume(ID remoteContainerID, Resume resume) {
		if (pauses.remove(remoteContainerID) && pauses.isEmpty())
			notify();
	}

	protected void handleMessage(ID remoteContainerID, Message message) {
		synchronized (this) {
			version = message.getVersion();
		}

		receiveMessage(message.getVersion(), message.getData());
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
	public void dispose(ID containerID) {
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
