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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

import com.mycorp.examples.timeservice.ITimeService;

public class Activator implements BundleActivator {

	private static final String GENERIC_SERVER_CONFIG = "ecf.generic.server";
	private static final String GENERIC_SERVER_IDPROP_NAME = GENERIC_SERVER_CONFIG+ ".id";
	private static final String GENERIC_SERVER_IDPROP_VALUE = "ecftcp://localhost:3288/server";
	
	private static final String REST_SERVER_CONFIG = "com.mycorp.examples.timeservice.rest.host";
	private static final String REST_SERVER_IDPROP_NAME = REST_SERVER_CONFIG + ".id";
	private static final String REST_SERVER_IDPROP_VALUE = "http://localhost:8181";
	
	public void start(BundleContext context) throws Exception {
		// If the verboseRemoteServiceAdmin system property is set
		// then register debug listener
		if (Boolean.getBoolean("verboseRemoteServiceAdmin"))
			registerDebugListener(context);

		// Create remote service properties...see createRemoteServiceProperties above
		Dictionary<String, Object> props = createRemoteServiceProperties();
		// Create MyTimeService impl and register as a remote service
		// register the remote service with the service registry. If ECF remote
		// services/RSA impl is installed and started, it will export this
		// service via the default distribution provider, which is
		// 'ecf.generic.server'
		// To change which provider is used (e.g.) r-OSGi:
		// props.put("service.exported.configs","ecf.r-osgi.peer");
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
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		// This is the only required service property to trigger remote services
		props.put("service.exported.interfaces", "*");
		// set service.exported.configs
		String serviceExportedConfig = System.getProperty("service.exported.configs",GENERIC_SERVER_CONFIG);
		props.put("service.exported.configs",serviceExportedConfig);
		String idPropName = null;
		String idPropValue = null;
		if (GENERIC_SERVER_CONFIG.equals(serviceExportedConfig)) {
			idPropName = GENERIC_SERVER_IDPROP_NAME;
			idPropValue = GENERIC_SERVER_IDPROP_VALUE;
		} else if (REST_SERVER_CONFIG.equals(serviceExportedConfig)) {
			idPropName = REST_SERVER_IDPROP_NAME;
			idPropValue = REST_SERVER_IDPROP_VALUE;
		} else throw new NullPointerException("Unsuppored value for service.exported.config="+serviceExportedConfig);
		
		// Set the id propName and idPropValue
		props.put(idPropName,idPropValue);
		return props;
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
		// Register as service, and RemoteServiceAdmin will callback
		context.registerService(RemoteServiceAdminListener.class.getName(),
				rsaListener, null);
	}

}
