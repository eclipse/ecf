/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultEndpointDescriptionAdvertiser;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.ExportRegistration;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionAdvertiser;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IHostContainerSelector;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.util.tracker.ServiceTracker;

public class TopologyManagerImpl extends AbstractTopologyManager implements EndpointListener {

	private DiscoveryImpl discovery;

	private ServiceRegistration endpointListenerRegistration;

	private DefaultEndpointDescriptionAdvertiser defaultEndpointDescriptionAdvertiser;
	private ServiceRegistration defaultEndpointDescriptionAdvertiserRegistration;
	private ServiceTracker endpointDescriptionAdvertiserTracker;
	private Object endpointDescriptionAdvertiserTrackerLock = new Object();

	public TopologyManagerImpl(BundleContext context, DiscoveryImpl discovery) {
		super(context);
		this.discovery = discovery;
	}

	public void start() throws Exception {
		// Register as EndpointListener, so that it gets notified when Endpoints
		// are discovered
		Properties props = new Properties();
		props.put(
				org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE,
				"(" + RemoteConstants.ENDPOINT_ID + "=*)");
		endpointListenerRegistration = getContext().registerService(
				EndpointListener.class.getName(), this, props);

		// Create default publisher
		defaultEndpointDescriptionAdvertiser = new DefaultEndpointDescriptionAdvertiser(discovery);
		// Register with minimum service ranking so others can customize
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		defaultEndpointDescriptionAdvertiserRegistration = getContext().registerService(
				IEndpointDescriptionAdvertiser.class.getName(),
				defaultEndpointDescriptionAdvertiser, properties);
		
	}

	public void close() {
		synchronized (endpointDescriptionAdvertiserTrackerLock) {
			if (endpointDescriptionAdvertiserTracker != null) {
				endpointDescriptionAdvertiserTracker.close();
				endpointDescriptionAdvertiserTracker = null;
			}
		}
		if (defaultEndpointDescriptionAdvertiserRegistration != null) {
			defaultEndpointDescriptionAdvertiserRegistration.unregister();
			defaultEndpointDescriptionAdvertiserRegistration = null;
		}
		if (defaultEndpointDescriptionAdvertiser != null) {
			defaultEndpointDescriptionAdvertiser.close();
			defaultEndpointDescriptionAdvertiser = null;
		}

		if (endpointListenerRegistration != null) {
			endpointListenerRegistration.unregister();
			endpointListenerRegistration = null;
		}
		discovery = null;
		super.close();
	}

	public void endpointAdded(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (endpoint instanceof EndpointDescription) {
			handleEndpointAdded((EndpointDescription) endpoint);
		} else
			logWarning("ECF Topology Manager:  Non-ECF endpointAdded="
					+ endpoint + ",matchedFilter=" + matchedFilter);
	}

	public void endpointRemoved(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpoint,
			String matchedFilter) {
		if (endpoint instanceof EndpointDescription) {
			handleEndpointRemoved((EndpointDescription) endpoint);
		} else
			logWarning("ECF Topology Manager:  Non-ECF endpointRemoved="
					+ endpoint + ",matchedFilter=" + matchedFilter);
	}

	protected IEndpointDescriptionAdvertiser getEndpointDescriptionAdvertiser() {
		synchronized (endpointDescriptionAdvertiserTrackerLock) {
			if (endpointDescriptionAdvertiserTracker == null) {
				endpointDescriptionAdvertiserTracker = new ServiceTracker(getContext(),
						IEndpointDescriptionAdvertiser.class.getName(), null);
				endpointDescriptionAdvertiserTracker.open();
			}
		}
		return (IEndpointDescriptionAdvertiser) endpointDescriptionAdvertiserTracker.getService();
	}

	private void handleEndpointAdded(EndpointDescription endpoint) {
		// TODO Auto-generated method stub
		trace("handleEndpointAdded", "endpoint=" + endpoint);
	}

	private void trace(String method, String message) {
		// TODO Auto-generated method stub
		System.out.println("TopologyManager." + method + ": " + message);
	}

	private void logWarning(String string) {
		System.out.println(string);
	}

	private void handleEndpointRemoved(EndpointDescription endpoint) {
		// TODO Auto-generated method stub
		trace("handleEndpointRemoved", "endpoint=" + endpoint);
	}

	private void handleServiceRegistered(ServiceReference serviceReference) {
		// Using OSGI 4.2 Chap 13 Remote Services spec, get the specified remote
		// interfaces for the given service reference
		String[] exportedInterfaces = getExportedInterfaces(serviceReference);
		// If no remote interfaces set, then we don't do anything with it
		if (exportedInterfaces == null)
			return;

		// Get optional service property for exported configs
		String[] exportedConfigs = getStringArrayFromPropertyValue(serviceReference
		.getProperty(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS));

		// Get all intents (service.intents, service.exported.intents,
		// service.exported.intents.extra)
		String[] serviceIntents = getServiceIntents(serviceReference);

		// Get a host container selector
		IHostContainerSelector hostContainerSelector = getHostContainerSelector();
		if (hostContainerSelector == null) {
			logError("selectRemoteServiceContainers","No hostContainerSelector available");
			return;
		}
		// select ECF remote service containers that match given exported interfaces, configs, and intents
		IRemoteServiceContainer[] rsContainers = hostContainerSelector.selectHostContainers(serviceReference, exportedInterfaces, exportedConfigs, serviceIntents);
		// If none found, log a warning and we're done
		if (rsContainers == null || rsContainers.length == 0) {
			logWarning(
					"handleServiceRegistered", //$NON-NLS-1$
					DebugOptions.TOPOLOGY_MANAGER, this.getClass(),
					"No remote service containers found for serviceReference=" //$NON-NLS-1$
							+ serviceReference + ". Remote service NOT EXPORTED"); //$NON-NLS-1$
			return;
		}
		// prepare export properties
		Map<String, Object> exportProperties = prepareExportProperties(serviceReference,exportedInterfaces,exportedConfigs,serviceIntents,rsContainers);
		
		// Select remote service admin
		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa = selectRemoteServiceAdmin(serviceReference,exportedInterfaces,exportedConfigs,serviceIntents,rsContainers);
		
		// if no remote service admin available, then log error and return
		if (rsa == null) {
			logError("handleServiceRegistered","No RemoteServiceAdmin found for serviceReference="+serviceReference+".  Remote service NOT EXPORTED");
			return;
		}
		// Export the remote service using the selected remote service admin
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> registrations = rsa.exportService(serviceReference, exportProperties);
		
		if (registrations.size() == 0) {
			logError("handleServiceRegistered","No export registrations created by RemoteServiceAdmin="+rsa+".  ServiceReference="+serviceReference+" NOT EXPORTED");
			return;
		}
		
		// publish exported registrations
		publishExportedRegistrations(registrations);
		
	}
	
	private void publishExportedRegistrations(
			Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> registrations) {
		for(org.osgi.service.remoteserviceadmin.ExportRegistration reg: registrations) {
			if (reg instanceof ExportRegistration) {
				publishExportedRegistration((ExportRegistration) reg);
			}
		}
	}

	private void publishExportedRegistration(ExportRegistration reg) {
		IEndpointDescriptionAdvertiser advertiser = getEndpointDescriptionAdvertiser();
		if (advertiser == null) {
			logError("advertiseExportedRegistration","No endpoint description advertiser available to advertise ExportRegistration="+reg);
			return;
		}
		// Now advertise endpoint description using endpoint description advertiser
		IStatus result = advertiser.advertise((EndpointDescription) reg.getExportReference().getExportedEndpoint());
		if (!result.isOK()) logError("advertiseExportedRegistration","Advertise of ExportRegistration="+reg+" FAILED",result);
	}

	private void logError(String method, String message, IStatus result) {
		// TODO Auto-generated method stub
		logError(method,method);
		
	}

	private Map<String, Object> prepareExportProperties(
			ServiceReference serviceReference, String[] exportedInterfaces,
			String[] exportedConfigs, String[] serviceIntents,
			IRemoteServiceContainer[] rsContainers) {
		// TODO Auto-generated method stub
		return null;
	}

	private void logError(String method, String message) {
		// TODO Auto-generated method stub
		
	}
	
}
