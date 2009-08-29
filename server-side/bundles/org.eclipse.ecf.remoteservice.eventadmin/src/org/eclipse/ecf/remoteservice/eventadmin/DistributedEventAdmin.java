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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

public class DistributedEventAdmin extends BaseSharedObject implements EventAdmin {

	private BundleContext context;
	
	private ServiceTracker eventHandlerServiceTracker;
	private final Object eventHandlerSTLock = new Object();

	private IExecutor executor;

	private String topic;
	private ID topicID;
	
	public DistributedEventAdmin(BundleContext context, String topic, IExecutor executor) {
		this.context = context;
		Assert.isNotNull(this.context);
		this.topic = topic;
		Assert.isNotNull(this.topic);
		this.topicID = IDFactory.getDefault().createStringID(getTopic());
		this.executor = (executor == null)?new ThreadsExecutor():executor;
	}
	
	public DistributedEventAdmin(BundleContext context, String topic) {
		this(context,topic,null);
	}

	public String getTopic() {
		return topic;
	}
	
	public ID getTopicID() {
		return topicID;
	}
	
	public void addToContainer(ISharedObjectContainer soContainer) throws SharedObjectAddException {
		soContainer.getSharedObjectManager().addSharedObject(topicID, this, null);
	}

	public void removeFromContainer(ISharedObjectContainer soContainer) {
		soContainer.getSharedObjectManager().removeSharedObject(getTopicID());
	}
	
	public ServiceRegistration register(Properties props) {
		if (props == null) props = new Properties();
		props.put(EventConstants.EVENT_TOPIC, getTopic());
		return this.context.registerService(EventAdmin.class.getName(), this, props);
	}
	
	public ServiceRegistration register() {
		return register(null);
	}
	
	public void dispose() {
		if (eventHandlerServiceTracker != null) {
			eventHandlerServiceTracker.close();
			eventHandlerServiceTracker = null;
		}
		topic = null;
		topicID = null;
		executor = null;
	}
	
	public void postEvent(final Event event) {
		try {
			sendSharedObjectMsgTo(null, SharedObjectMsg.createMsg(
					"handlePostEvent", new DistributedEventAdminMessage(event)));
			localPostEvent(event);
		} catch (IOException e) {
			logError("postEvent exception event=" + event + " not sent.", e);
		}
	}

	public void sendEvent(Event event) {
		try {
			sendSharedObjectMsgTo(null, SharedObjectMsg.createMsg(
					"handleSendEvent", new DistributedEventAdminMessage(event)));
			localSendEvent(event);
		} catch (IOException e) {
			logError("sendEvent exception event=" + event + " not sent.", e);
		}
	}

	private ServiceReference[] getEventHandlerServiceReferences(Event event) {
		synchronized (eventHandlerSTLock) {
			if (eventHandlerServiceTracker == null) {
				eventHandlerServiceTracker = new ServiceTracker(context,
						EventHandler.class.getName(), null);
				eventHandlerServiceTracker.open();
			}
			ServiceReference[] refs = eventHandlerServiceTracker
					.getServiceReferences();
			List results = new ArrayList();
			if (refs == null)
				return null;
			for (int i = 0; i < refs.length; i++) {
				String eventFilter = (String) refs[i]
						.getProperty(EventConstants.EVENT_FILTER);
				Filter filter = null;
				if (eventFilter != null) {
					try {
						filter = context.createFilter(eventFilter);
						if (event.matches(filter))
							results.add(refs[i]);
					} catch (InvalidSyntaxException e) {
						logError("getEventHandlers eventFilter=" + eventFilter,
								e);
						continue;
					}
				} else
					results.add(refs[i]);
			}
			return (ServiceReference[]) results
					.toArray(new ServiceReference[] {});
		}
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

	private void localSendEvent(Event event) {
		ServiceReference[] eventHandlerRefs = getEventHandlerServiceReferences(event);
		if (eventHandlerRefs == null) {
			logWarning("sendEvent event=" + event
					+ ".  No service references found to post to.");
			return;
		}
		// Now synchronously call every eventhandler
		for (int i = 0; i < eventHandlerRefs.length; i++)
			callSync(eventHandlerRefs[i], (EventHandler) context
					.getService(eventHandlerRefs[i]), event);

	}

	private void localPostEvent(Event event) {
		ServiceReference[] refs = getEventHandlerServiceReferences(event);
		if (refs == null) {
			logWarning("localPostEvent event=" + event
					+ ".  No service references found to post to.");
			return;
		}
		for (int i = 0; i < refs.length; i++)
			fireAsync(refs[i], (EventHandler) context.getService(refs[i]),
					event);
	}

	void handlePostEvent(DistributedEventAdminMessage event) {
		localPostEvent(event.getEvent());
	}

	void handleSendEvent(DistributedEventAdminMessage event) {
		localSendEvent(event.getEvent());
	}

	protected void logWarning(String string) {
		System.out.println(string);
	}

	protected void callSync(final ServiceReference serviceReference,
			final EventHandler eventHandler, final Event event) {
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable exception) {
				logError(
						"Exception in EventHandler.handleEvent. eventHandler="
								+ eventHandler + ".  serviceReference="
								+ serviceReference + ".  event=" + event,
						exception);
			}

			public void run() throws Exception {
				eventHandler.handleEvent(event);
			}
		});
	}

	protected void fireAsync(final ServiceReference serviceReference,
			final EventHandler eventHandler, final Event event) {
		executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor arg0) throws Exception {
				eventHandler.handleEvent(event);
				return null;
			}
		}, null);
	}

	protected void logError(String string, Throwable e) {
		System.err.println(string);
		if (e != null)
			e.printStackTrace(System.err);
	}

}
