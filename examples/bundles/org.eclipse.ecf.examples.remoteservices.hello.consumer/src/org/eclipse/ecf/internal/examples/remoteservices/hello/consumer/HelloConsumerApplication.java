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
package org.eclipse.ecf.internal.examples.remoteservices.hello.consumer;

import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceProxy;
import org.eclipse.ecf.remoteservice.RemoteServiceHelper;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class HelloConsumerApplication implements IApplication,
		IDistributionConstants, ServiceTrackerCustomizer {

	public static final String CONSUMER_NAME = "org.eclipse.ecf.examples.remoteservices.hello.consumer";

	private BundleContext bundleContext;
	private ServiceTracker containerFactoryServiceTracker;

	private String containerType = "ecf.r_osgi.peer";

	private final Object appLock = new Object();
	private boolean done = false;

	private ServiceTracker helloServiceTracker;

	public Object start(IApplicationContext appContext) throws Exception {
		// Set bundle context (for use with service trackers)
		bundleContext = Activator.getContext();
		processArgs(appContext);

		// Create ECF container of appropriate type. The container instance
		// can be created in a variety of ways...e.g. via code like the line below, 
		// via the new org.eclipse.ecf.container extension point, or automatically 
		// upon discovery via the IProxyContainerFinder/DefaultProxyContainerFinder.  
		getContainerFactory().createContainer(containerType);

		// Create service tracker to track IHello instances that have the 'service.imported'
		// property set (as defined by OSGi 4.2 remote services spec).
		helloServiceTracker = new ServiceTracker(bundleContext,
				createRemoteFilter(), this);
		helloServiceTracker.open();

		startLocalDiscoveryIfPresent();
		
		waitForDone();

		return IApplication.EXIT_OK;
	}

	private void startLocalDiscoveryIfPresent() {
		Bundle[] bundles = bundleContext.getBundles();
		for(int i=0; i < bundles.length; i++) {
			if (bundles[i].getSymbolicName().equals("org.eclipse.ecf.osgi.services.discovery.local")) {
				try {
					bundles[i].start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Filter createRemoteFilter() throws InvalidSyntaxException {
		// This filter looks for IHello instances that have the 
		// 'service.imported' property set, as specified by OSGi 4.2
		// remote services spec (Chapter 13)
		return bundleContext.createFilter("(&("
				+ org.osgi.framework.Constants.OBJECTCLASS + "="
				+ IHello.class.getName() + ")(" + SERVICE_IMPORTED + "=*))");
	}

	public void stop() {
		if (helloServiceTracker != null) {
			helloServiceTracker.close();
			helloServiceTracker = null;
		}
		if (containerFactoryServiceTracker != null) {
			containerFactoryServiceTracker.close();
			containerFactoryServiceTracker = null;
		}
		this.bundleContext = null;
	}

	private IContainerFactory getContainerFactory() {
		if (containerFactoryServiceTracker == null) {
			containerFactoryServiceTracker = new ServiceTracker(bundleContext,
					IContainerFactory.class.getName(), null);
			containerFactoryServiceTracker.open();
		}
		return (IContainerFactory) containerFactoryServiceTracker.getService();
	}

	private void processArgs(IApplicationContext appContext) {
		String[] originalArgs = (String[]) appContext.getArguments().get(
				"application.args");
		if (originalArgs == null)
			return;
		for (int i = 0; i < originalArgs.length; i++) {
			if (originalArgs[i].equals("-containerType")) {
				containerType = originalArgs[i + 1];
				i++;
			}
		}
	}

	private void waitForDone() {
		// then just wait here
		synchronized (appLock) {
			while (!done) {
				try {
					appLock.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * Method called when a REMOTE IHello instance is registered.
	 */
	public Object addingService(ServiceReference reference) {
		System.out.println("IHello service proxy being added");
		// Since this reference is for a remote service,
		// The service object returned is a proxy implementing the
		// IHello interface
		IHello hello = (IHello) bundleContext.getService(reference);
		// This makes a remote 'hello' call
		hello.hello(CONSUMER_NAME);
		System.out.println("Completed hello remote service invocation using proxy");

		// OSGi 4.2 remote service spec requires a property named 'service.imported' to be
		// set to a non-null value.  In the case of any ECF provider, this 'service.imported' property
		// is set to the IRemoteService object associated with the remote service.
		IRemoteService remoteServiceViaProperty = (IRemoteService) reference
				.getProperty(IDistributionConstants.SERVICE_IMPORTED);
		// This IRemoteService instance allows allows non-blocking/asynchronous invocation of
		// remote methods.  This allows the client to decide (at runtime if necessary) whether
		// to do synchronous/blocking calls or asynchronous/non-blocking calls.
		
		// In this case, we will make an non-blocking call and immediately get a 'future'...which is
		// a placeholder for a result of the remote computation.  This will not block.
		IFuture future = RemoteServiceHelper.futureExec(remoteServiceViaProperty, "hello",
				new Object[] { CONSUMER_NAME + " future" });
		// Client can execute arbitrary code here...
		try {
			// This blocks until communication and computation have completed successfully
			future.get();
			System.out.println("Completed hello remote service invocation using future");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Now get the IRemoteService from the proxy
		IRemoteService remoteServiceViaProxy = ((IRemoteServiceProxy) hello).getRemoteService();
		// Create listener for asynchronous callback
		IRemoteCallListener listener = new IRemoteCallListener() {
			public void handleEvent(IRemoteCallEvent event) {
				if (event instanceof IRemoteCallCompleteEvent) {
					System.out.println("Completed hello remote service invocation using async");
				}
			}};
		// Call asynchronously with listener
		RemoteServiceHelper.asyncExec(remoteServiceViaProxy, "hello", new Object[] { CONSUMER_NAME + " async" }, listener);
		
		return hello;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
	}

}
