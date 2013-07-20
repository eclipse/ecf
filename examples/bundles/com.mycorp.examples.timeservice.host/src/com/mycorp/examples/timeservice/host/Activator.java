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

	public void start(BundleContext context) throws Exception {
		// If the verboseRemoteServiceAdmin system property is set
		// then register debug listener
		if (Boolean.getBoolean("verboseRemoteServiceAdmin"))
			registerDebugListener(context);

		// Create MyTimeService impl and register as a remote service
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		// This is the only required service property to trigger remote services
		props.put("service.exported.interfaces", "*");
		// set service.exported.configs
		props.put("service.exported.configs",System.getProperty("service.exported.configs","ecf.generic.server"));
		// Set the ecf-generic-provider-specific id
		props.put("ecf.generic.server.id",System.getProperty("ecf.generic.server.id","ecftcp://localhost:3288/server"));
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
