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

import java.util.Properties;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultConsumerContainerSelector;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DefaultEndpointDescriptionAdvertiser;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IConsumerContainerSelector;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionAdvertiser;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteConstants;
import org.osgi.util.tracker.ServiceTracker;

public class TopologyManagerImpl extends AbstractTopologyManager implements EndpointListener {

	private DiscoveryImpl discovery;
	private RemoteServiceAdminImpl remoteServiceAdminImpl;

	private ServiceRegistration endpointListenerRegistration;

	private DefaultEndpointDescriptionAdvertiser defaultPublisher;
	private ServiceRegistration defaultPublisherRegistration;
	private ServiceTracker publisherTracker;
	private Object publisherTrackerLock = new Object();

	private DefaultConsumerContainerSelector defaultConsumerContainerSelector;
	private ServiceRegistration defaultConsumerContainerSelectorRegistration;
	private ServiceTracker consumerContainerTracker;
	private Object consumerContainerTrackerLock = new Object();
	
	public TopologyManagerImpl(BundleContext context, DiscoveryImpl discovery) {
		super(context);
		this.discovery = discovery;
		this.remoteServiceAdminImpl = new RemoteServiceAdminImpl(context, this);
	}

	public void start() throws Exception {
		// Register as EndpointListener, so that it gets notified when Endpoints
		// are discovered
		Properties props = new Properties();
		props.put(
				org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE,
				"(" + RemoteConstants.ENDPOINT_ID + "=*)");
		endpointListenerRegistration = context.registerService(
				EndpointListener.class.getName(), this, props);

		// Create default publisher
		defaultPublisher = new DefaultEndpointDescriptionAdvertiser(discovery);
		// Register with minimum service ranking so others can customize
		final Properties properties = new Properties();
		properties.put(Constants.SERVICE_RANKING,
				new Integer(Integer.MIN_VALUE));
		defaultPublisherRegistration = context.registerService(
				IEndpointDescriptionAdvertiser.class.getName(),
				defaultPublisher, properties);
		
		// Register consumer container selector
		defaultConsumerContainerSelector = new DefaultConsumerContainerSelector();
		defaultConsumerContainerSelectorRegistration = context.registerService(IConsumerContainerSelector.class.getName(), defaultConsumerContainerSelector, null);
		
	}

	public void close() {
		synchronized (consumerContainerTrackerLock) {
			if (consumerContainerTracker != null) {
				consumerContainerTracker.close();
				consumerContainerTracker = null;
			}
		}
		if (defaultConsumerContainerSelectorRegistration != null) {
			defaultConsumerContainerSelectorRegistration.unregister();
			defaultConsumerContainerSelectorRegistration = null;
		}
		if (defaultConsumerContainerSelector != null) {
			defaultConsumerContainerSelector.close();
			defaultConsumerContainerSelector = null;
		}
		synchronized (publisherTrackerLock) {
			if (publisherTracker != null) {
				publisherTracker.close();
				publisherTracker = null;
			}
		}
		if (defaultPublisherRegistration != null) {
			defaultPublisherRegistration.unregister();
			defaultPublisherRegistration = null;
		}
		if (defaultPublisher != null) {
			defaultPublisher.close();
			defaultPublisher = null;
		}

		if (endpointListenerRegistration != null) {
			endpointListenerRegistration.unregister();
			endpointListenerRegistration = null;
		}
		if (remoteServiceAdminImpl != null) {
			remoteServiceAdminImpl.close();
			remoteServiceAdminImpl = null;
		}
		remoteServiceAdminImpl = null;
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

	protected IEndpointDescriptionAdvertiser getEndpointDescriptionPublisher() {
		synchronized (publisherTrackerLock) {
			if (publisherTracker == null) {
				publisherTracker = new ServiceTracker(context,
						IEndpointDescriptionAdvertiser.class.getName(), null);
				publisherTracker.open();
			}
		}
		return (IEndpointDescriptionAdvertiser) publisherTracker.getService();
	}

	protected IConsumerContainerSelector getConsumerContainerSelector() {
		synchronized (consumerContainerTrackerLock) {
			if (consumerContainerTracker != null) {
				consumerContainerTracker = new ServiceTracker(context, IConsumerContainerSelector.class.getName(), null);
				consumerContainerTracker.open();
			}
		}
		return (IConsumerContainerSelector) consumerContainerTracker.getService();
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

}
