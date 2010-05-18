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
package org.eclipse.ecf.internal.examples.remoteservices.hello.host;

import java.util.Properties;

import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.examples.remoteservices.hello.impl.Hello;
import org.eclipse.ecf.osgi.services.discovery.IHostDiscoveryListener;
import org.eclipse.ecf.osgi.services.discovery.LoggingHostDiscoveryListener;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.eclipse.ecf.osgi.services.distribution.IHostDistributionListener;
import org.eclipse.ecf.osgi.services.distribution.LoggingHostDistributionListener;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class HelloHostApplication implements IApplication,
		IDistributionConstants {

	private static final String DEFAULT_CONTAINER_TYPE = "ecf.r_osgi.peer";
	public static final String DEFAULT_CONTAINER_ID = "r-osgi://localhost:9278";

	private BundleContext bundleContext;

	private String containerType = DEFAULT_CONTAINER_TYPE;
	private String containerId = DEFAULT_CONTAINER_ID;

	private final Object appLock = new Object();
	private boolean done = false;

	private ServiceRegistration helloRegistration;
	//private ServiceRegistration helloRegistration2;
	
	public Object start(IApplicationContext appContext) throws Exception {
		
		bundleContext = Activator.getContext();
		
		
		// Process Arguments
		processArgs(appContext);
		
		// Register host discovery listener to log the publish/unpublish of remote services.  
		// This LoggingHostDiscoveryListener logs the publication of OSGi remote services...so 
		// that the discovery can be more easily debugged.
		// Note that other IHostDiscoveryListener may be created and registered, and
		// all will be notified of publish/unpublish events
		bundleContext.registerService(IHostDiscoveryListener.class.getName(), new LoggingHostDiscoveryListener(), null);
		
		// Register host distribution listener to log the register/unregister of remote services.  
		// This LoggingHostDistributionListener logs the register/unregister of OSGi remote services...so 
		// that the distribution can be more easily debugged.
		// Note that other IHostDistributionListener may be created and registered, and
		// all will be notified of register/unregister events
		bundleContext.registerService(IHostDistributionListener.class.getName(), new LoggingHostDistributionListener(), null);

		helloRegistration =	startService(bundleContext, containerType, containerId);

		// Register the console command
		new HelloCommand(bundleContext, helloRegistration, containerType, containerId);
		
		// wait until stopped
		waitForDone();

		return IApplication.EXIT_OK;
	}

	public static ServiceRegistration startService(BundleContext bundleContext, String containerType, String containerId) {
		// Setup properties for remote service distribution, as per OSGi 4.2 remote services
		// specification (chap 13 in compendium spec)
		Properties props = new Properties();
		// add OSGi service property indicated export of all interfaces exposed by service (wildcard)
		props.put(IDistributionConstants.SERVICE_EXPORTED_INTERFACES, IDistributionConstants.SERVICE_EXPORTED_INTERFACES_WILDCARD);
		// add OSGi service property specifying config
		props.put(IDistributionConstants.SERVICE_EXPORTED_CONFIGS, containerType);
		// add ECF service property specifying container factory args
		props.put(IDistributionConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGUMENTS, containerId);
		// register remote service
		ServiceRegistration reg = bundleContext.registerService(IHello.class
				.getName(), new Hello(), props);
		// tell everyone
		System.out.println("Host: Hello Service Registered");
		
		return reg;
	}

	public static ServiceRegistration stopService(ServiceRegistration helloRegistration)
	{
		helloRegistration.unregister();

		// tell everyone
		System.out.println("Host: Hello Service Unregistered");
		
		return null;
	}
	public void stop() {
		if (helloRegistration != null) {
			helloRegistration.unregister();
			helloRegistration = null;
		}
//		if (helloRegistration2 != null) {
//			helloRegistration2.unregister();
//			helloRegistration2 = null;
//		}
		bundleContext = null;
		synchronized (appLock) {
			done = true;
			appLock.notifyAll();
		}
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
			} else if (originalArgs[i].equals("-containerId")) {
				containerId = originalArgs[i + 1];
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

}
