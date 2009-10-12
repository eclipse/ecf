/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.eventadmin;

import java.io.IOException;
import java.security.Permission;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.sharedobject.BaseSharedObject;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.internal.remoteservice.eventadmin.DistributedEventAdminMessage;
import org.eclipse.ecf.internal.remoteservice.eventadmin.EventHandlerTracker;
import org.eclipse.ecf.internal.remoteservice.eventadmin.EventHandlerWrapper;
import org.eclipse.ecf.internal.remoteservice.eventadmin.LogTracker;
import org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap;
import org.eclipse.osgi.framework.eventmgr.EventManager;
import org.eclipse.osgi.framework.eventmgr.ListenerQueue;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.TopicPermission;
import org.osgi.service.log.LogService;

public class DistributedEventAdmin extends BaseSharedObject implements
		EventAdmin {

	private LogTracker log;
	private EventHandlerTracker eventHandlerTracker;
	private EventManager eventManager;
	
	public DistributedEventAdmin(BundleContext context) {
		Assert.isNotNull(context);
		this.log = new LogTracker(context, System.out);
		this.eventHandlerTracker = new EventHandlerTracker(context, log);
	}

	public void start() {
		log.open();
		ThreadGroup eventGroup = new ThreadGroup("Distributed EventAdmin"); //$NON-NLS-1$
		eventGroup.setDaemon(true);
		eventManager = new EventManager("Distributed EventAdmin Async Event Dispatcher Thread", eventGroup);
		eventHandlerTracker.open();
	}
	
	public void stop() {
		eventHandlerTracker.close();
		eventManager.close();
		eventManager = null;
		log.close();
	}

	public void postEvent(final Event event) {
		try {
			sendSharedObjectMsgTo(null, SharedObjectMsg.createMsg(
					"handlePostEvent", new DistributedEventAdminMessage(event)));
		} catch (IOException e) {
			logError(NLS.bind(
					"IOException posting distributed event {0} to {1}", event,
					event.getTopic()), e);
		}
		localDispatch(event, true);
	}

	public void sendEvent(Event event) {
		localDispatch(event, false);
	}

	protected boolean handleSharedObjectMsg(SharedObjectMsg msg) {
		try {
			msg.invoke(this);
			return true;
		} catch (final Exception e) {
			logError("handleSharedObjectMsg invoke error on msg=" + msg, e);
		}
		return false;
	}

	protected void localDispatch(Event event, boolean isAsync) {
		EventManager currentManager = eventManager;
		if (currentManager == null) {
			return;
		}

		if (event == null) {
			log.log(LogService.LOG_ERROR,
					"Null event passed to EventAdmin was ignored.");
		}

		String eventTopic = event.getTopic();

		try {
			SecurityManager sm = System.getSecurityManager();
			if (sm != null)
				sm.checkPermission(new TopicPermission(eventTopic,
						TopicPermission.PUBLISH));
		} catch (SecurityException e) {
			String msg = NLS
					.bind(
							"Caller bundle does not have TopicPermission to publish topic {0}",
							eventTopic);
			logError(msg, e);
			throw e;
		}

		Set eventHandlerWrappers = eventHandlerTracker.getHandlers(eventTopic);

		SecurityManager sm = System.getSecurityManager();
		Permission perm = (sm == null) ? null : new TopicPermission(eventTopic,
				TopicPermission.SUBSCRIBE);

		CopyOnWriteIdentityMap listeners = new CopyOnWriteIdentityMap();
		Iterator iter = eventHandlerWrappers.iterator();
		while (iter.hasNext()) {
			EventHandlerWrapper wrapper = (EventHandlerWrapper) iter.next();
			listeners.put(wrapper, perm);
		}
		
		ListenerQueue listenerQueue = new ListenerQueue(currentManager);
		listenerQueue.queueListeners(listeners.entrySet(), eventHandlerTracker);
		if (isAsync) {
			listenerQueue.dispatchEventAsynchronous(0, event);
		}
		else {
			listenerQueue.dispatchEventSynchronous(0, event);
		}
	}

	void handlePostEvent(DistributedEventAdminMessage event) {
		localDispatch(event.getEvent(), true);
	}

	protected void logError(String message, Throwable exception) {
		if (log != null) {
			log.log(LogService.LOG_ERROR, message, exception);
		} else {
			System.err.println(message);
			if (exception != null)
				exception.printStackTrace(System.err);
		}
	}

}
