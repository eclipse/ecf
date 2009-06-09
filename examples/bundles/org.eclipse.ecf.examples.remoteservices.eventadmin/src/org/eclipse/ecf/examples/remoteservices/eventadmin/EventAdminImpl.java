package org.eclipse.ecf.examples.remoteservices.eventadmin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;

public class EventAdminImpl implements EventAdmin {

	private BundleContext context;
	private IExecutor executor;
	private Object executorLock = new Object();
	private ServiceTracker eventHandlerServiceTracker;
	private Object eventHandlerSTLock = new Object();
	private long defaultTimeout;

	public EventAdminImpl(BundleContext context, IExecutor executor,
			long defaultTimeout) {
		this.context = context;
		this.executor = executor;
		this.defaultTimeout = defaultTimeout;
	}

	private IRemoteService getRemoteServiceForServiceReference(
			ServiceReference ref) {
		if (ref == null)
			return null;
		Object remoteProp = ref.getProperty(IDistributionConstants.REMOTE);
		if (remoteProp != null && remoteProp instanceof IRemoteService)
			return (IRemoteService) remoteProp;
		return null;
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
				try {
					filter = context.createFilter(eventFilter);
				} catch (InvalidSyntaxException e) {
					logError("getEventHandlers eventFilter=" + eventFilter, e);
					continue;
				}
				if (event.matches(filter))
					results.add(refs[i]);
			}
			return (ServiceReference[]) results
					.toArray(new ServiceReference[] {});
		}
	}

	private void logError(String string, Throwable e) {
		System.err.println(string);
		if (e != null)
			e.printStackTrace(System.err);
	}

	public void postEvent(final Event event) {
		ServiceReference[] refs = getEventHandlerServiceReferences(event);
		if (refs == null) {
			logWarning("postEvent event=" + event
					+ ".  No service references found to post to.");
			return;
		}
		for (int i = 0; i < refs.length; i++) {
			IRemoteService remoteService = getRemoteServiceForServiceReference(refs[i]);
			// If this is a remote service, then fire the EventHandler
			// asynchronously
			if (remoteService != null)
				fireAsync(refs[i], remoteService, createRemoteCall(event));
			// Else call the EventHandler asynchronously
			else
				fireAsync(refs[i], (EventHandler) context.getService(refs[i]),
						event);
		}
	}

	private void logWarning(String string) {
		System.out.println(string);
	}

	private IRemoteCall createRemoteCall(final Event event) {
		return new IRemoteCall() {

			public String getMethod() {
				return "handleEvent";
			}

			public Object[] getParameters() {
				return new Object[] { event };
			}

			public long getTimeout() {
				return defaultTimeout;
			}
		};
	}

	public void sendEvent(Event event) {
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

	private void callSync(final ServiceReference serviceReference,
			final EventHandler eventHandler, final Event event) {
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable exception) {
				logCallException(
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

	private void fireAsync(final ServiceReference serviceReference,
			final EventHandler eventHandler, final Event event) {
		getExecutor().execute(new IProgressRunnable() {
			public Object run(IProgressMonitor arg0) throws Exception {
				eventHandler.handleEvent(event);
				return null;
			}
		}, null);
	}

	private IExecutor getExecutor() {
		synchronized (executorLock) {
			if (executor == null) {
				executor = new ThreadsExecutor();
			}
			return executor;
		}
	}

	private void fireAsync(final ServiceReference serviceReference,
			final IRemoteService remoteService, final IRemoteCall call) {
		SafeRunner.run(new ISafeRunnable() {
			public void handleException(Throwable exception) {
				logCallException(
						"Exception in IRemoteService.fireAsync. IRemoteService="
								+ remoteService + ".  serviceReference="
								+ serviceReference + ". remoteCall=" + call,
						exception);
			}

			public void run() throws Exception {
				remoteService.fireAsync(call);
			}
		});
	}

	private void logCallException(String string, Throwable t) {
		System.err.println(string);
		if (t != null)
			t.printStackTrace(System.err);
	}

	public void dispose() {
		synchronized (eventHandlerSTLock) {
			if (eventHandlerServiceTracker != null) {
				eventHandlerServiceTracker.close();
				eventHandlerServiceTracker = null;
			}
		}
		synchronized (executorLock) {
			executor = null;
		}
		context = null;
	}
}
