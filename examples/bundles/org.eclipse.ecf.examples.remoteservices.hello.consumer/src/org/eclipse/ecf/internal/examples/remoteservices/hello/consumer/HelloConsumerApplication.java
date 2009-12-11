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
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.RemoteServiceHelper;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class HelloConsumerApplication implements IApplication,
		IDistributionConstants, ServiceTrackerCustomizer {

	public static final String CONSUMER_NAME = "org.eclipse.ecf.examples.remoteservices.hello.consumer";

	private static final String DEFAULT_CONTAINER_TYPE = "ecf.r_osgi.peer";

	private BundleContext bundleContext;
	private ServiceTracker containerManagerServiceTracker;

	private String containerType = DEFAULT_CONTAINER_TYPE;

	private final Object appLock = new Object();
	private boolean done = false;

	private ServiceTracker helloServiceTracker;

	public Object start(IApplicationContext appContext) throws Exception {
		// Set bundle context (for use with service trackers)
		bundleContext = Activator.getContext();
		processArgs(appContext);

		// Create ECF container. This setup is required so that an ECF provider
		// will
		// be available for handling discovered remote endpoints
		createContainer();

		// Create service tracker to track IHello instances that are REMOTE
		helloServiceTracker = new ServiceTracker(bundleContext,
				createRemoteFilter(), this);
		helloServiceTracker.open();

		waitForDone();

		return IApplication.EXIT_OK;
	}

	private void createContainer() throws Exception {
		// Get container factory
		IContainerFactory containerFactory = getContainerManagerService()
				.getContainerFactory();
		containerFactory.createContainer(containerType);
	}

	private Filter createRemoteFilter() throws InvalidSyntaxException {
		// This filter looks for IHello instances that have the REMOTE property
		// set (are remote
		// services as per RFC119).
		return bundleContext.createFilter("(&("
				+ org.osgi.framework.Constants.OBJECTCLASS + "="
				+ IHello.class.getName() + ")(" + SERVICE_IMPORTED + "=*))");
	}

	public void stop() {
		if (helloServiceTracker != null) {
			helloServiceTracker.close();
			helloServiceTracker = null;
		}
		if (containerManagerServiceTracker != null) {
			containerManagerServiceTracker.close();
			containerManagerServiceTracker = null;
		}
		this.bundleContext = null;
	}

	private IContainerManager getContainerManagerService() {
		if (containerManagerServiceTracker == null) {
			containerManagerServiceTracker = new ServiceTracker(bundleContext,
					IContainerManager.class.getName(), null);
			containerManagerServiceTracker.open();
		}
		return (IContainerManager) containerManagerServiceTracker.getService();
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
		IHello hello = (IHello) bundleContext.getService(reference);
		// Call it
		hello.hello(CONSUMER_NAME);
		System.out.println("Called hello using proxy");

		// Now get remote service reference and use asynchronous
		// remote invocation
		IRemoteService remoteService = (IRemoteService) reference
				.getProperty(SERVICE_IMPORTED);

		// This futureExec returns immediately
		IFuture future = RemoteServiceHelper.futureExec(remoteService, "hello",
				new Object[] { CONSUMER_NAME + " future" });

		try {
			// This method blocks until a return
			future.get();
			System.out.println("Called hello using future");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hello;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
	}

}
