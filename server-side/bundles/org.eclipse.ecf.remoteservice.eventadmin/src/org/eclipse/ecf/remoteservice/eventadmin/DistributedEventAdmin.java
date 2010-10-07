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
import java.io.NotSerializableException;
import java.security.Permission;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.BaseSharedObject;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectCreateResponseEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.internal.remoteservice.eventadmin.EventHandlerTracker;
import org.eclipse.ecf.internal.remoteservice.eventadmin.EventHandlerWrapper;
import org.eclipse.ecf.internal.remoteservice.eventadmin.LogTracker;
import org.eclipse.osgi.framework.eventmgr.CopyOnWriteIdentityMap;
import org.eclipse.osgi.framework.eventmgr.EventManager;
import org.eclipse.osgi.framework.eventmgr.ListenerQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.TopicPermission;
import org.osgi.service.log.LogService;

public class DistributedEventAdmin extends BaseSharedObject implements
		EventAdmin {

	private LogTracker logTracker;
	private LogService log;
	private EventHandlerTracker eventHandlerTracker;
	private EventManager eventManager;
	
	private static final String SHARED_OBJECT_MESSAGE_METHOD = "__handlePostEventSharedObjectMsg";
	
	/**
	 * @since 1.1
	 */
	public DistributedEventAdmin(BundleContext context, LogService log) {
		Assert.isNotNull(context);
		if (log == null) {
			// create log tracker and set the log to it
			this.logTracker = new LogTracker(context, System.out);
			this.log = this.logTracker;
		} else {
			this.logTracker = null;
			this.log = log;
		}
		// Now create eventHandler tracker
		this.eventHandlerTracker = new EventHandlerTracker(context, log);
	}
	
	public DistributedEventAdmin(BundleContext context) {
		this(context, null);
	}

	public void start() {
		if (logTracker != null) logTracker.open();
		ThreadGroup eventGroup = new ThreadGroup("Distributed EventAdmin"); //$NON-NLS-1$
		eventGroup.setDaemon(true);
		eventManager = new EventManager("Distributed EventAdmin Async Event Dispatcher Thread", eventGroup);
		eventHandlerTracker.open();
	}
	
	public void stop() {
		eventHandlerTracker.close();
		eventManager.close();
		eventManager = null;
		if (logTracker != null) logTracker.close();
	}

	public void sendEvent(Event event) {
		localDispatch(event, false);
	}

	/**
	 * @since 1.1
	 */
	public void postEvent(final Event event) {
		// First thing, we allow subclasses to decide whether the given event should be translated before message
		// send into a new Event, or if it should not be sent at all
		Event eventToSend = getEventToSend(event);
		if (eventToSend != null) {
			sendMessage(eventToSend);
			// sent successfully, so now dispatch to any appropriate local EventHandlers
			notifyPostSendMessage(eventToSend);
			// This does local dispatch asynchronously
			localDispatch(event, true);
		}
	}

	/**
	 * @since 1.1
	 */
	protected void sendMessage(Event eventToSend) {
		ID target = null;
		Object[] messageData = null;
		try {
			target = getTarget(eventToSend);
			messageData = createMessageDataFromEvent(target, eventToSend);
			sendSharedObjectMsgTo(target, SharedObjectMsg.createMsg(
					SHARED_OBJECT_MESSAGE_METHOD, messageData));				
		} catch (IOException e) {
			handleSendMessageException("send exception to target="+target,eventToSend, messageData, e);
		}
	}

	/**
	 * @since 1.1
	 */
	protected Object[] createMessageDataFromEvent(ID target, Event eventToSend) throws NotSerializableException {
		Object[] results = { new EventMessage(eventToSend) };
		return results;
	}

	/**
	 * @since 1.1
	 */
	protected Event createEventFromMessageData(ID fromID, Object[] messageData) {
		EventMessage eventMessage = (EventMessage) messageData[0];
		return eventMessage.getEvent();
	}


	/**
	 * @since 1.1
	 */
	protected void handleSendMessageException(String message, Event eventToSend, Object[] messageParams, IOException exception) {
		String exceptionMessage = ((message==null)?"":message)+" eventToSend="+eventToSend+" messageParams="+((messageParams==null)?null:Arrays.asList(messageParams));
		logError(exceptionMessage,exception);
		// By default we throw a runtime exception
		throw new ServiceException(exceptionMessage,exception);
	}
	
	/**
	 * @since 1.1
	 */
	protected ID getTarget(Event eventToSend) {
		return null;
	}
	
	/**
	 * @since 1.1
	 */
	protected Event getEventToSend(Event event) {
		// By default, we distribute the same event that is passed in
		return event;
	}

	/**
	 * @since 1.1
	 */
	protected void notifyPostSendMessage(Event eventToSend) {
	}

	
	/**
	 * @since 1.1
	 */
	protected Event notifyPreLocalDispatch(Event event) {
		return event;
	}

	/**
	 * @since 1.1
	 */
	protected void notifyPostLocalDispatch(Event event) {
	}

	protected void localDispatch(Event dispatchedEvent, boolean isAsync) {
		EventManager currentManager = eventManager;
		if (currentManager == null) {
			return;
		}

		if (dispatchedEvent == null) {
			log.log(LogService.LOG_ERROR,
					"Null event passed to EventAdmin was ignored.");
		}

		Event event = notifyPreLocalDispatch(dispatchedEvent);
		
		if (event != null) {
			
			String eventTopic = event.getTopic();

			try {
				SecurityManager sm = System.getSecurityManager();
				if (sm != null)
					sm.checkPermission(new TopicPermission(eventTopic,
							TopicPermission.PUBLISH));
			} catch (SecurityException e) {
				logError("Caller bundle does not have TopicPermission to publish topic "+eventTopic, e);
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
			
			notifyPostLocalDispatch(event);
			
		}
	}

	/**
	 * @since 1.1
	 */
	protected boolean handleSharedObjectMsg(ID fromID, SharedObjectMsg msg) {
		String soMethod = msg.getMethod();
		if (SHARED_OBJECT_MESSAGE_METHOD.equals(soMethod)) {
			try {
				Object[] messageData = msg.getParameters();
				Event receivedEvent = createEventFromMessageData(fromID, messageData);
				if (receivedEvent != null) {
					notifyReceivedEvent(fromID, receivedEvent);
					localDispatch(receivedEvent, true);
				}
			} catch (Exception e) {
				logError("DistributedEventAdmin handleSharedObjectMsg error receiving msg="+msg,e);
			}
			return true;
		} else {
			logError("DistributedEventAdmin received bad shared object msg="+msg+" from="+fromID);
		}
		return false;
	}
	
	/**
	 * @since 1.1
	 */
	protected void notifyReceivedEvent(ID fromID, Event receivedEvent) {
	}

	/**
	 * @since 1.1
	 */
	protected final boolean handleSharedObjectMsgEvent(ISharedObjectMessageEvent event) {
		boolean result = false;
		if (event instanceof ISharedObjectCreateResponseEvent)
			result = handleSharedObjectCreateResponseEvent((ISharedObjectCreateResponseEvent) event);
		else {
			SharedObjectMsg msg = getSharedObjectMsgFromEvent(event);
			if (msg != null)
				result = handleSharedObjectMsg(event.getRemoteContainerID(), msg);
		}
		return result;
	}

	// log methods
	/**
	 * @since 1.1
	 */
	protected void logWarning(String message) {
		logWarning(message,null);
	}
	/**
	 * @since 1.1
	 */
	protected void logWarning(String message, Throwable exception) {
		if (log != null) {
			log.log(LogService.LOG_WARNING, message, exception);
		} else {
			System.out.println(message);
			if (exception != null)
				exception.printStackTrace(System.out);
		}
	}
	/**
	 * @since 1.1
	 */
	protected void logError(String message) {
		logError(message,null);
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
