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

import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.RemoteServiceHelper;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator, IDistributionConstants, ServiceTrackerCustomizer {

	public static final String CONSUMER_NAME = "org.eclipse.ecf.examples.remoteservices.hello.consumer";
	
	private String containerType = System.getProperty("containerType","ecf.r_osgi.peer");
	private BundleContext context;
	private ServiceTracker containerManagerServiceTracker;
	private ServiceTracker helloServiceTracker;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		// Create R-OSGi Container
		IContainerManager containerManager = getContainerManagerService();
		containerManager.getContainerFactory().createContainer(containerType);
		// Create service tracker to track IHello instances that are REMOTE
		helloServiceTracker = new ServiceTracker(context,createRemoteFilter(),this);
		helloServiceTracker.open();
	}

	private Filter createRemoteFilter() throws InvalidSyntaxException {
		// This filter looks for IHello instances that have the REMOTE property set (are remote 
		// services as per RFC119).
		return context.createFilter("(&("+org.osgi.framework.Constants.OBJECTCLASS+"=" + IHello.class.getName() +")(" + REMOTE + "=*))");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		helloServiceTracker.close();
		helloServiceTracker = null;
		if (containerManagerServiceTracker != null) {
			containerManagerServiceTracker.close();
			containerManagerServiceTracker = null;
		}
		this.context = null;
	}

	/**
	 * Method called when a REMOTE IHello instance is registered.
	 */
	public Object addingService(ServiceReference reference) {
		System.out.println("IHello service proxy being added");
		IHello hello = (IHello) context.getService(reference);
		// Call it
		hello.hello(CONSUMER_NAME);
		System.out.println("Called hello using proxy");
		
		// Now get remote service reference and use asynchronous 
		// remote invocation
		IRemoteService remoteService = (IRemoteService) reference.getProperty(REMOTE);
		
		// This futureExec returns immediately
		IFuture future = RemoteServiceHelper.futureExec(remoteService, "hello", new Object[] { CONSUMER_NAME + " future" });
		
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

	private IContainerManager getContainerManagerService() {
		if (containerManagerServiceTracker == null) {
			containerManagerServiceTracker = new ServiceTracker(context, IContainerManager.class.getName(),null);
			containerManagerServiceTracker.open();
		}
		return (IContainerManager) containerManagerServiceTracker.getService();
	}


}
