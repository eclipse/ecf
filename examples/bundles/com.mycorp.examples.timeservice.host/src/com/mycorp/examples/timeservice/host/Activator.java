/*******************************************************************************
 * Copyright (c) 2013 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package com.mycorp.examples.timeservice.host;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

import com.mycorp.examples.timeservice.ITimeService;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		// If the verboseRemoteServiceAdmin system property is set
		// then register debug listener
		if (Boolean.getBoolean("verboseRemoteServiceAdmin"))
			registerDebugListener(context);

		// Create remote service properties...see createRemoteServiceProperties()
		Dictionary<String, Object> props = createRemoteServiceProperties();
		
		// Create MyTimeService impl and register/export as a remote service
		ServiceRegistration<ITimeService> timeServiceRegistration = context
				.registerService(ITimeService.class, new TimeServiceImpl(),
						props);
		
		// Print out that ITimeService remote service registration
		System.out.println("MyTimeService host registered with registration="
				+ timeServiceRegistration);
	}

	public void stop(BundleContext context) throws Exception {
		// do nothing
	}

	private Dictionary<String,Object> createRemoteServiceProperties() {
		// This is the only required service property to trigger remote services
		Dictionary<String,Object> result = new Hashtable<String,Object>();
		result.put("service.exported.interfaces", "*");
		Properties props = System.getProperties();
		String config = props.getProperty("service.exported.configs");
		if (config != null) {
			result.put("service.exported.configs", config);
			String configProps = config + ".";
			for(Object k: props.keySet()) {
				if (k instanceof String) {
					String key = (String) k;
					if (key.startsWith(configProps) || key.equals("ecf.exported.async.interfaces")) result.put(key, props.getProperty(key));
				}
			}
		}
		return result;
	}

	// Register a RemoteServiceAdminListener so we can report to sdtout
	// when a remote service has actually been successfully exported by
	// the RSA implementation
	private void registerDebugListener(BundleContext context) {
		RemoteServiceAdminListener rsaListener = new RemoteServiceAdminListener() {
			public void remoteAdminEvent(RemoteServiceAdminEvent event) {
				switch (event.getType()) {
				case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
					System.out
							.println("Service Exported by RemoteServiceAdmin.  EndpointDescription Properties="
									+ event.getExportReference()
											.getExportedEndpoint()
											.getProperties());
				}
			}

		};
		// Register our listener as service via whiteboard pattern, and RemoteServiceAdmin will callback
		context.registerService(RemoteServiceAdminListener.class.getName(),
				rsaListener, null);
	}

}
