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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;

import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectContainerDepartedEvent;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.internal.datashare.DataSharePlugin;

/**
 * @author pnehrer
 */
public class ConsistentMulticaster extends AbstractMulticaster implements
		Timeout.Listener {

	public static final short SEND = 3;

	public static final short RECEIVE = 4;

	public static final long DEFAULT_TIMEOUT = 1000;

	public static final String TRACE_TAG = "ConsistentMulticaster";

	private long sendTimeout;

	private long receiveTimeout;

	private Version nextVersion;

	private HashSet requests;

	private final Timer timer = new Timer();

	private final HashMap timeouts = new HashMap();

	private final HashMap commits = new HashMap();

	private boolean granted;

	public synchronized boolean sendMessage(Object message) throws ECFException {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "sendMessage[message=" + message + "]");

		try {
			if (!waitToSend())
				return false;

			state = SEND;
			nextVersion = new Version(localContainerID,
					version.getSequence() + 1);
			ArrayList others = new ArrayList(Arrays.asList(context
					.getGroupMemberIDs()));
			others.remove(localContainerID);
			requests = new HashSet(others);
			granted = true;
			try {
				context.sendMessage(null, new Request(nextVersion));
				wait(sendTimeout);
				if (state != SEND)
					return false;

				if (!granted || !requests.isEmpty()) {
					context.sendMessage(null, new Abort(nextVersion));
					return false;
				}

				requests.addAll(others);
				context.sendMessage(null, new Message(nextVersion, message));
				wait(sendTimeout);
				if (state != SEND)
					return false;

				if (!requests.isEmpty()) {
					context.sendMessage(null, new Abort(nextVersion));
					return false;
				}

				context.sendMessage(null, new Commit(nextVersion));
				version = nextVersion;
				return true;
			} catch (IOException e) {
				throw new ECFException(e);
			} catch (InterruptedException e) {
				throw new ECFException(e);
			} finally {
				state = READY;
				notify();
			}
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
	}

	public String getStateStr() {
		switch (state) {
		case SEND:
			return "SND";
		case RECEIVE:
			return "RCV";
		default:
			return super.getStateStr();
		}
	}

	protected void receiveMessage(Object message) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
	 */
	public synchronized void init(ISharedObjectConfig config)
			throws SharedObjectInitException {
		super.init(config);

		sendTimeout = DEFAULT_TIMEOUT;
		receiveTimeout = DEFAULT_TIMEOUT;

		Map params = config.getProperties();
		if (params != null) {
			Object param = params.get("sendTimeout");
			if (param instanceof Long)
				sendTimeout = ((Long) param).longValue();

			param = params.get("receiveTimeout");
			if (param instanceof Long)
				receiveTimeout = ((Long) param).longValue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		super.handleEvent(event);
		if (event instanceof ISharedObjectMessageEvent) {
			ISharedObjectMessageEvent e = (ISharedObjectMessageEvent) event;
			if (e.getData() instanceof Request)
				handleRequest(e.getRemoteContainerID(), (Request) e.getData());
			else if (e.getData() instanceof Reply)
				handleReply(e.getRemoteContainerID(), (Reply) e.getData());
			else if (e.getData() instanceof Abort)
				handleAbort(e.getRemoteContainerID(), (Abort) e.getData());
			else if (e.getData() instanceof Ack)
				handleAck(e.getRemoteContainerID(), (Ack) e.getData());
			else if (e.getData() instanceof Commit)
				handleCommit(e.getRemoteContainerID(), (Commit) e.getData());
		}
	}

	protected void handleDeparted(ISharedObjectContainerDepartedEvent event) {
		super.handleDeparted(event);
		if (!event.getDepartedContainerID().equals(localContainerID)) {
			synchronized (this) {
				if (state == SEND) {
					requests.remove(event.getDepartedContainerID());
					if (requests.isEmpty())
						notify();
				} else if (state == RECEIVE) {
					Timeout[] t = (Timeout[]) timeouts.values().toArray(
							new Timeout[timeouts.size()]);
					for (int i = 0; i < t.length; ++i) {
						if (t[i].getVersion().getSenderID().equals(
								event.getDepartedContainerID())
								&& t[i].cancel())
							timeouts.remove(t[i].getVersion());
					}

					for (Iterator i = commits.keySet().iterator(); i.hasNext();) {
						Version version = (Version) i.next();
						if (version.getSenderID().equals(
								event.getDepartedContainerID()))
							i.remove();
					}

					if (timeouts.isEmpty()) {
						state = READY;
						notify();
					}
				}
			}
		}
	}

	private synchronized void handleRequest(ID remoteContainerID,
			Request request) {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "handleRequest[remoteContainerID="
					+ remoteContainerID + ";request=" + request + "]");

		try {
			if ((state == READY || state == RECEIVE)
					&& version.getSequence() + 1 == request.getVersion()
							.getSequence()) {
				state = RECEIVE;
				Timeout timeout = new Timeout(this, request.getVersion());
				timeouts.put(request.getVersion(), timeout);
				timer.schedule(timeout, receiveTimeout);
				context.sendMessage(remoteContainerID, new Reply(request
						.getVersion(), true));
			} else if (state != DISPOSED)
				context.sendMessage(remoteContainerID, new Reply(request
						.getVersion(), false));
		} catch (IOException e) {
			DataSharePlugin.log(e);
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
	}

	private synchronized void handleReply(ID remoteContainerID, Reply reply) {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "handleReply[remoteContainerID="
					+ remoteContainerID + ";reply=" + reply + "]");

		try {
			if (state == SEND && reply.getVersion().equals(nextVersion)) {
				if (!reply.isGranted()) {
					granted = false;
					notify();
				}

				requests.remove(remoteContainerID);
				if (requests.isEmpty())
					notify();
			}
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
	}

	protected synchronized void handleMessage(ID remoteContainerID,
			Message message) {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "handleMessage[remoteContainerID="
					+ remoteContainerID + ";message=" + message + "]");

		try {
			Timeout timeout;
			if (((timeout = (Timeout) timeouts.get(message.getVersion())) != null)
					&& timeout.cancel()) {
				commits.put(message.getVersion(), message.getData());
				timeout = new Timeout(this, message.getVersion());
				timeouts.put(message.getVersion(), timeout);
				timer.schedule(timeout, 1000);
				context.sendMessage(remoteContainerID, new Ack(message
						.getVersion()));
			}
		} catch (IOException e) {
			DataSharePlugin.log(e);
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
	}

	private synchronized void handleAbort(ID remoteContainerID, Abort abort) {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "handleAbort[remoteContainerID="
					+ remoteContainerID + ";abort=" + abort + "]");

		try {
			Timeout timeout = (Timeout) timeouts.remove(abort.getVersion());
			if (timeout != null && timeout.cancel()) {
				commits.remove(abort.getVersion());
				if (timeouts.isEmpty()) {
					state = READY;
					notify();
				}
			}
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
	}

	private synchronized void handleAck(ID remoteContainerID, Ack ack) {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "handleAck[remoteContainerID="
					+ remoteContainerID + ";ack=" + ack + "]");
		try {
			if (state == SEND && ack.getVersion().equals(nextVersion)) {
				requests.remove(remoteContainerID);
				if (requests.isEmpty())
					notify();
			}
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
	}

	private synchronized void handleCommit(ID remoteContainerID, Commit commit) {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "handleCommit[remoteContainerID="
					+ remoteContainerID + ";commit=" + commit + "]");

		try {
			Timeout timeout;
			if (((timeout = (Timeout) timeouts.get(commit.getVersion())) != null)
					&& timeout.cancel()) {
				version = commit.getVersion();
				timeouts.remove(version);
				if (timeouts.isEmpty()) {
					state = READY;
					notify();
				}

				receiveMessage(commits.remove(version));
			}
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
	}

	public synchronized void timeout(Version version) {
		String method = null;
		if (DataSharePlugin.isTracing(TRACE_TAG))
			traceEntry(method = "timeout[version=" + version + "]");

		try {
			timeouts.remove(version);
			if (timeouts.isEmpty()) {
				commits.remove(version);
				state = READY;
				notify();
			}
		} finally {
			if (DataSharePlugin.isTracing(TRACE_TAG))
				traceExit(method);
		}
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
