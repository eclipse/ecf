/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceRegistration;

public class RemoteServiceRegistration {

	private final IRemoteServiceContainerAdapter containerAdapter;
	private final IRemoteServiceReference remoteReference;
	private final ServiceRegistration serviceRegistration;

	public RemoteServiceRegistration(IRemoteServiceContainerAdapter adapter,
			IRemoteServiceReference remoteReference, ServiceRegistration reg) {
		this.containerAdapter = adapter;
		this.remoteReference = remoteReference;
		this.serviceRegistration = reg;
	}

	public IRemoteServiceContainerAdapter getContainerAdapter() {
		return containerAdapter;
	}

	public IRemoteServiceReference getRemoteReference() {
		return remoteReference;
	}

	public ServiceRegistration getServiceRegistration() {
		return serviceRegistration;
	}
}
