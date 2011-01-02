/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class EndpointListenerTrackerCustomizer implements
		ServiceTrackerCustomizer {

	private BundleContext context;
	private EndpointDescriptionLocator endpointDescriptionLocator;

	public EndpointListenerTrackerCustomizer(BundleContext context,
			EndpointDescriptionLocator endpointDescriptionLocator) {
		this.context = context;
		this.endpointDescriptionLocator = endpointDescriptionLocator;
	}

	public Object addingService(ServiceReference reference) {
		Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> allDiscoveredEndpointDescriptions = endpointDescriptionLocator
				.getAllDiscoveredEndpointDescriptions();
		if (context == null)
			return null;
		EndpointListener listener = (EndpointListener) context
				.getService(reference);
		if (listener == null)
			return null;
		for (org.osgi.service.remoteserviceadmin.EndpointDescription ed : allDiscoveredEndpointDescriptions) {
			EndpointDescriptionLocator.EndpointListenerHolder[] endpointListenerHolders = endpointDescriptionLocator
					.getMatchingEndpointListenerHolders(
							new ServiceReference[] { reference }, ed);
			if (endpointListenerHolders != null) {
				for (int i = 0; i < endpointListenerHolders.length; i++) {
					endpointDescriptionLocator.queueEndpointDescription(
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
