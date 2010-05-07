/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.distribution;

import org.eclipse.ecf.osgi.services.discovery.IRemoteServiceEndpointDescription;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceRegistration;

public class LoggingProxyDistributionListener extends
		AbstractDistributionListener implements IProxyDistributionListener {

	public void retrievingRemoteServiceReferences(
			IRemoteServiceEndpointDescription endpointDescription,
			IRemoteServiceContainer remoteServiceContainer) {
		if (endpointDescription == null || remoteServiceContainer == null)
			return;
		StringBuffer sb = new StringBuffer(
				"OSGi ECF service distribution: retrievingRemoteServiceReferences").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append(createTabs(1))
				.append("endpointDescription=").append(endpointDescription).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append(createTabs(1)).append("remoteServiceContainer") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(printRemoteServiceContainer(2, remoteServiceContainer));
		sb.append("\n"); //$NON-NLS-1$
		log(null, sb.toString(), null);
	}

	public void registering(
			IRemoteServiceEndpointDescription endpointDescription,
			IRemoteServiceContainer remoteServiceContainer,
			IRemoteServiceReference remoteServiceReference) {
		if (endpointDescription == null || remoteServiceContainer == null
				|| remoteServiceReference == null)
			return;
		StringBuffer sb = new StringBuffer(
				"OSGi ECF service distribution: registering").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append(createTabs(1))
				.append("endpointDescription=").append(endpointDescription).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append(createTabs(1)).append("remoteServiceContainer") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(printRemoteServiceContainer(2, remoteServiceContainer));
		sb.append("\n"); //$NON-NLS-1$
		sb.append(createTabs(1)).append("remoteServiceReference") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(printRemoteServiceReference(2, remoteServiceReference));
		sb.append("\n"); //$NON-NLS-1$
		log(null, sb.toString(), null);

	}

	public void registered(
			IRemoteServiceEndpointDescription endpointDescription,
			IRemoteServiceContainer remoteServiceContainer,
			IRemoteServiceReference remoteServiceReference,
			ServiceRegistration proxyServiceRegistration) {
		if (endpointDescription == null || remoteServiceContainer == null
				|| remoteServiceReference == null
				|| proxyServiceRegistration == null)
			return;
		StringBuffer sb = new StringBuffer(
				"OSGi ECF service distribution: registered").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append(createTabs(1))
				.append("endpointDescription=").append(endpointDescription).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$

		sb.append(createTabs(1)).append("remoteServiceContainer") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(printRemoteServiceContainer(2, remoteServiceContainer));
		sb.append("\n"); //$NON-NLS-1$
		sb.append(createTabs(1)).append("remoteServiceReference") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(printRemoteServiceReference(2, remoteServiceReference));
		sb.append(createTabs(1)).append("proxyServiceRegistration=") //$NON-NLS-1$
				.append(proxyServiceRegistration);
		sb.append("\n"); //$NON-NLS-1$
		log(null, sb.toString(), null);
	}

	public void unregistered(
			IRemoteServiceEndpointDescription endpointDescription,
			ServiceRegistration proxyServiceRegistration) {

		StringBuffer sb = new StringBuffer(
				"OSGi ECF service distribution: unregistered").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(createTabs(1))
				.append("endpointDescription=").append(endpointDescription).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(createTabs(1)).append("proxyServiceRegistration=") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(proxyServiceRegistration);
		sb.append("\n"); //$NON-NLS-1$
		log(null, sb.toString(), null);

	}

}
