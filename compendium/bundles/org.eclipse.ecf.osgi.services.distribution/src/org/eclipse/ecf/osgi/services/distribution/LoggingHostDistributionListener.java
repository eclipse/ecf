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

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceReference;

public class LoggingHostDistributionListener extends
		AbstractDistributionListener implements IHostDistributionListener {

	public void registered(ServiceReference serviceReference,
			IRemoteServiceContainer remoteServiceContainer,
			IRemoteServiceRegistration remoteRegistration) {

		if (serviceReference == null || remoteServiceContainer == null
				|| remoteRegistration == null)
			return;

		StringBuffer sb = new StringBuffer(
				"OSGi ECF service distribution: registered").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(createTabs(1)).append("serviceReference=") //$NON-NLS-1$
				.append(serviceReference);
		sb.append("\n"); //$NON-NLS-1$
		sb.append(createTabs(1)).append("remoteServiceContainer") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(printRemoteServiceContainer(2, remoteServiceContainer));
		sb.append("\n"); //$NON-NLS-1$
		sb.append(createTabs(1)).append("remoteServiceRegistration") //$NON-NLS-1$
				.append("\n") //$NON-NLS-1$
				.append(printRemoteServiceRegistration(2, remoteRegistration));
		log(serviceReference, sb.toString(), null);
	}

	public void unregistered(ServiceReference serviceReference,
			IRemoteServiceRegistration remoteRegistration) {
		if (serviceReference == null || remoteRegistration == null)
			return;

		StringBuffer sb = new StringBuffer(
				"OSGi ECF service distribution: unregistered").append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append(createTabs(1)).append("serviceReference=") //$NON-NLS-1$
				.append(serviceReference);
		sb.append("\n"); //$NON-NLS-1$
		sb.append(createTabs(1)).append("remoteServiceRegistration") //$NON-NLS-1$
				.append(printRemoteServiceRegistration(2, remoteRegistration));
		sb.append("\n"); //$NON-NLS-1$
		log(serviceReference, sb.toString(), null);
	}

}
