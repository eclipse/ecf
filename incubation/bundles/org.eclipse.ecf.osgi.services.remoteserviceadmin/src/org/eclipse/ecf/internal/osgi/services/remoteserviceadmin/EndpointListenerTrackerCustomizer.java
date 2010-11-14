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

import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class EndpointListenerTrackerCustomizer implements
		ServiceTrackerCustomizer {

	private DiscoveryImpl discovery;

	public EndpointListenerTrackerCustomizer(DiscoveryImpl discovery) {
		this.discovery = discovery;
	}

	public Object addingService(ServiceReference reference) {
		Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> allDiscoveredEndpointDescriptions = discovery
				.getAllDiscoveredEndpointDescriptions();
		EndpointListener listener = (EndpointListener) Activator.getContext()
				.getService(reference);
		if (listener == null)
			return null;
		for (org.osgi.service.remoteserviceadmin.EndpointDescription ed : allDiscoveredEndpointDescriptions) {
			DiscoveryImpl.EndpointListenerHolder[] endpointListenerHolders = discovery
					.getMatchingEndpointListenerHolders(
							new ServiceReference[] { reference }, ed);
			if (endpointListenerHolders != null) {
				for (int i = 0; i < endpointListenerHolders.length; i++) {
					discovery.queueEndpointDescription(
							endpointListenerHolders[i].getListener(),
							endpointListenerHolders[i].getDescription(),
							endpointListenerHolders[i].getMatchingFilter(),
							true);
				}
			}
		}
		return listener;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
	}

	public void close() {
	}

}
