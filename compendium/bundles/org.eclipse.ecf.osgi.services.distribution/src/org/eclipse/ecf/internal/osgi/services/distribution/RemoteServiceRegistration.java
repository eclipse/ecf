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

import java.util.*;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.osgi.services.discovery.RemoteServiceEndpointDescription;
import org.eclipse.ecf.remoteservice.*;
import org.osgi.framework.ServiceRegistration;

public class RemoteServiceRegistration {

	private final RemoteServiceEndpointDescription serviceEndpointDescription;
	private final IRemoteServiceContainer rsContainer;
	private final IRemoteServiceListener listener;
	private Map serviceRegistrations = new HashMap();

	public RemoteServiceRegistration(RemoteServiceEndpointDescription sed,
			IRemoteServiceContainer rsContainer, IRemoteServiceListener l) {
		this.serviceEndpointDescription = sed;
		this.rsContainer = rsContainer;
		this.listener = l;
		getContainerAdapter().addRemoteServiceListener(this.listener);
	}

	public RemoteServiceEndpointDescription getServiceEndpointDescription() {
		return serviceEndpointDescription;
	}

	public IContainer getContainer() {
		return rsContainer.getContainer();
	}

	public IRemoteServiceContainerAdapter getContainerAdapter() {
		return rsContainer.getContainerAdapter();
	}

	public void dispose() {
		getContainerAdapter().removeRemoteServiceListener(this.listener);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("RemoteServiceRegistration[");
		buf.append("sed=").append(getServiceEndpointDescription());
		buf.append(";containerID=").append(getContainer().getID()).append("]");
		return buf.toString();
	}

	public void addServiceRegistration(IRemoteServiceReference ref,
			ServiceRegistration registration) {
		List l = (List) serviceRegistrations.get(ref.getID());
		if (l == null) {
			l = new ArrayList();
			serviceRegistrations.put(ref.getID(), l);
		}
		l.add(registration);
	}

	public ServiceRegistration[] removeServiceRegistration(
			IRemoteServiceReference reference) {
		if (getContainerAdapter().ungetRemoteService(reference)) {
			List l = (List) serviceRegistrations.remove(reference.getID());
			if (l != null)
				return (ServiceRegistration[]) l
						.toArray(new ServiceRegistration[] {});
		}
		return null;
	}

	public List removeAllServiceRegistrations() {
		List results = new ArrayList();
		for (Iterator i = serviceRegistrations.keySet().iterator(); i.hasNext();) {
			List l = (List) serviceRegistrations.get(i.next());
			if (l != null)
				results.addAll(l);
		}
		return results;
	}

	public boolean isEmpty() {
		return serviceRegistrations.size() == 0;
	}
}
