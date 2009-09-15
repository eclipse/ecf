/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservice.eventadmin;

import java.io.IOException;
import java.security.Permission;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.BaseSharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.core.sharedobject.SharedObjectAddException;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.internal.remoteservice.eventadmin.DistributedEventAdminMessage;
import org.eclipse.ecf.internal.remoteservice.eventadmin.EventHandlerTracker;
import org.eclipse.ecf.internal.remoteservice.eventadmin.EventHandlerWrapper;
import org.eclipse.ecf.internal.remoteservice.eventadmin.LogTracker;
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.TopicPermission;
import org.osgi.service.log.LogService;

public class DistributedEventAdmin extends BaseSharedObject implements
		EventAdmin {

	private BundleContext context;
	private String topic;
	private ID topicID;
	private LogTracker log;

	private EventHandlerTracker eventHandlerTracker;
	private IExecutor executor;

	public DistributedEventAdmin(BundleContext context, String topic,
			IExecutor executor) {
		this.context = context;
		Assert.isNotNull(this.context);
		this.topic = topic;
		Assert.isNotNull(this.topic);
		this.topicID = IDFactory.getDefault().createStringID(getTopic());
		this.log = new LogTracker(context, System.out);
		this.executor = (executor == null) ? new ThreadsExecutor() : executor;
		this.eventHandlerTracker = new EventHandlerTracker(context, log);
		this.eventHandlerTracker.open();
	}

	public DistributedEventAdmin(BundleContext context, String topic) {
		this(context, topic, null);
	}

	public String getTopic() {
		return topic;
	}

	public ID getTopicID() {
		return topicID;
	}

	public void addToContainer(ISharedObjectContainer soContainer)
			throws SharedObjectAddException {
		soContainer.getSharedObjectManager().addSharedObject(topicID, this,
				null);
	}

	public void removeFromContainer(ISharedObjectContainer soContainer) {
		soContainer.getSharedObjectManager().removeSharedObject(getTopicID());
	}

	public ServiceRegistration register(Properties props) {
		if (props == null)
			props = new Properties();
		props.put(EventConstants.EVENT_TOPIC, getTopic());
		return this.context.registerService(EventAdmin.class.getName(), this,
				props);
	}

	public ServiceRegistration register() {
		return register(null);
	}

	public void dispose() {
		if (this.eventHandlerTracker != null) {
			this.eventHandlerTracker.close();
		}
		if (this.log != null) {
			this.log.close();
		}
		topic = null;
		topicID = null;
		executor = null;
	}

	public void postEvent(final Event event) {
		try {
			sendSharedObjectMsgTo(null, SharedObjectMsg.createMsg(
					"handlePostEvent", new DistributedEventAdminMessage(event)));
		} catch (IOException e) {
			logError(NLS.bind(
					"IOException posting distributed event {0} to {1}", event,
					topic), e);
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
		IExecutor exec = executor;
		if (exec == null)
			return;

		if (event == null) {
			log.log(LogService.LOG_ERROR,
					"Null event passed to EventAdmin was ignored.");
			// continue from here will result in an NPE below; the spec for
			// EventAdmin does not allow for null here
		}

		String eventTopic = event.getTopic();

		try {
			SecurityManager sm = System.getSecurityManager();
			if (sm != null)
				sm.checkPermission(new TopicPermission(topic,
						TopicPermission.PUBLISH));
		} catch (SecurityException e) {
			String msg = NLS
					.bind(
							"Caller bundle does not have TopicPermission to publish topic {0}",
							eventTopic);
			logError(msg, e);
			// must throw a security exception here according to the EventAdmin
			// spec
			throw e;
		}

		Set eventHandlerWrappers = eventHandlerTracker.getHandlers(eventTopic);

		SecurityManager sm = System.getSecurityManager();
		Permission perm = (sm == null) ? null : new TopicPermission(topic,
				TopicPermission.SUBSCRIBE);

		// Now synchronously call every eventhandler
		if (isAsync)
			fireAsync(exec, eventHandlerWrappers, event, perm);
		else
			callSync(eventHandlerWrappers, event, perm);

	}

	void handlePostEvent(DistributedEventAdminMessage event) {
		localDispatch(event.getEvent(), true);
	}

	protected void callSync(Set eventHandlerWrappers, final Event event,
			final Permission perm) {
		for (Iterator i = eventHandlerWrappers.iterator(); i.hasNext();) {
			final EventHandlerWrapper wrapper = (EventHandlerWrapper) i.next();
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logError(
							NLS
									.bind(
											"Exception while dispatching event {0} to handler {1}",
											event, wrapper), exception);
				}

				public void run() throws Exception {
					wrapper.handleEvent(event, perm);
				}
			});
		}
	}

	protected void fireAsync(IExecutor exec, Set eventHandlerWrappers,
			final Event event, final Permission perm) {
		for (Iterator i = eventHandlerWrappers.iterator(); i.hasNext();) {
			final EventHandlerWrapper wrapper = (EventHandlerWrapper) i.next();
			exec.execute(new IProgressRunnable() {
				public Object run(IProgressMonitor arg0) throws Exception {
					wrapper.handleEvent(event, perm);
					return null;
				}
			}, null);
		}
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
