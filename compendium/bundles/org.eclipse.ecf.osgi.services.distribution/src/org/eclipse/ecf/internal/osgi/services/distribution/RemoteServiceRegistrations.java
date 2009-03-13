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
import org.eclipse.ecf.remoteservice.*;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.discovery.ServiceEndpointDescription;

public class RemoteServiceRegistrations {

	private final ServiceEndpointDescription serviceEndpointDescription;
	private final IContainer container;
	private final IRemoteServiceContainerAdapter containerAdapter;
	private IRemoteServiceListener listener;
	private Map serviceRegistrations = new HashMap();

	public RemoteServiceRegistrations(ServiceEndpointDescription sed, IContainer c,
			IRemoteServiceContainerAdapter adapter, IRemoteServiceListener l) {
		this.serviceEndpointDescription = sed;
		this.container = c;
		this.containerAdapter = adapter;
		this.listener = l;
		this.containerAdapter.addRemoteServiceListener(this.listener);
	}

	public ServiceEndpointDescription getServiceEndpointDescription() {
		return serviceEndpointDescription;
	}

	public IContainer getContainer() {
		return container;
	}

	public IRemoteServiceContainerAdapter getContainerAdapter() {
		return containerAdapter;
	}

	public void dispose() {
		this.containerAdapter.removeRemoteServiceListener(this.listener);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("RemoteServiceRegistrations[");
		buf.append("containerID=").append(getContainer().getID());
		buf.append(";rsca=").append(getContainerAdapter()).append("]");
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
			if (l != null) {
				return (ServiceRegistration[]) l
						.toArray(new ServiceRegistration[] {});
			}
		}
		return null;
	}

	public List removeAllServiceRegistrations() {
		List results = new ArrayList();
		for (Iterator i = serviceRegistrations.keySet().iterator(); i.hasNext();) {
			List l = (List) serviceRegistrations.get(i.next());
			if (l != null) {
				results.addAll(l);
			}
		}
		return results;
	}

	public boolean isEmpty() {
		return serviceRegistrations.size() == 0;
	}
}
