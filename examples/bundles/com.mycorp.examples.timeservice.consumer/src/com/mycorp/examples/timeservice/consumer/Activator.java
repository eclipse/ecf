/*******************************************************************************
 * Copyright (c) 2013 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package com.mycorp.examples.timeservice.consumer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.mycorp.examples.timeservice.ITimeService;

public class Activator implements BundleActivator, ServiceTrackerCustomizer<ITimeService,ITimeService> {

	private BundleContext context;
	private ServiceTracker<ITimeService,ITimeService> timeServiceTracker;
	
	public void start(BundleContext context) throws Exception {
		this.context = context;
		// If the verboseRemoteServiceAdmin system property is set
		// then register debug listener
		if (Boolean.getBoolean("verboseRemoteServiceAdmin"))
			registerDebugListener(context);
		
		// Create and open ITimeService tracker
		this.timeServiceTracker = new ServiceTracker<ITimeService,ITimeService>(this.context,ITimeService.class,this);
		this.timeServiceTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		if (timeServiceTracker != null) {
			timeServiceTracker.close();
			timeServiceTracker = null;
		}
	}

	/**
	 * NOTE:  The method will be called when the ITimeService is discovered.
	 */
	public ITimeService addingService(
			ServiceReference<ITimeService> reference) {
		// XXX Here is where the ITimeService is received, when discovered.
		System.out.println("ITimeService discovered!");
		System.out.println("Service Reference="+reference);
		// Get the time service proxy
		ITimeService timeService = this.context.getService(reference);
		System.out.println("Calling timeService="+timeService);
		// Call the service!
		Long time = timeService.getCurrentTime();
		// Print out the result
		System.out.println("Call Done.  Current time given by ITimeService.getCurrentTime() is: "+time);
		return timeService;
	}

	public void modifiedService(ServiceReference<ITimeService> reference,
			ITimeService service) {
		// do nothing
	}

	public void removedService(ServiceReference<ITimeService> reference,
			ITimeService service) {
		// do nothing
	}

	// Register a RemoteServiceAdminListener so we can report to sdtout
	// when a remote service has actually been successfully exported by
	// the RSA implementation
	private void registerDebugListener(BundleContext context) {
		RemoteServiceAdminListener rsaListener = new RemoteServiceAdminListener() {
			public void remoteAdminEvent(RemoteServiceAdminEvent event) {
				switch (event.getType()) {
				case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
					System.out
							.println("Service Imported by RemoteServiceAdmin.  EndpointDescription Properties="
									+ event.getImportReference().getImportedEndpoint().getProperties());
				}
			}

		};
		// Register as service, and RemoteServiceAdmin will callback
		context.registerService(RemoteServiceAdminListener.class.getName(),
				rsaListener, null);
	}


}
