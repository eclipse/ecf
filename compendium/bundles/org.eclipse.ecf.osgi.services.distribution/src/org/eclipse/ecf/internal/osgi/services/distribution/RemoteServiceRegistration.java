/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.osgi.services.discovery.RemoteServiceEndpointDescription;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceRegistration;

public class RemoteServiceRegistration {

	private IRemoteServiceContainer rsContainer;
	private IRemoteServiceListener listener;
	private Map serviceRegistrations = new HashMap();

	public RemoteServiceRegistration(IRemoteServiceContainer rsContainer,
			IRemoteServiceListener l) {
		Assert.isNotNull(rsContainer);
		Assert.isNotNull(l);
		this.rsContainer = rsContainer;
		this.listener = l;
		getContainerAdapter().addRemoteServiceListener(this.listener);
	}

	IContainer getContainer() {
		return rsContainer.getContainer();
	}

	IRemoteServiceContainerAdapter getContainerAdapter() {
		return rsContainer.getContainerAdapter();
	}

	void dispose() {
		synchronized (serviceRegistrations) {
			if (listener != null) {
				getContainerAdapter()
						.removeRemoteServiceListener(this.listener);
				this.listener = null;
			}
			if (rsContainer != null) {
				rsContainer = null;
			}
			serviceRegistrations.clear();
		}
	}

	class RSEDAndSRAssoc {
		RemoteServiceEndpointDescription rsed;
		ServiceRegistration sr;

		public RSEDAndSRAssoc(RemoteServiceEndpointDescription rsed,
				ServiceRegistration sr) {
			this.rsed = rsed;
			this.sr = sr;
		}

		public RemoteServiceEndpointDescription getRSED() {
			return rsed;
		}

		public ServiceRegistration getSR() {
			return sr;
		}
	}

	void addServiceRegistration(IRemoteServiceReference ref,
			RemoteServiceEndpointDescription rsed,
			ServiceRegistration registration) {
		synchronized (serviceRegistrations) {
			List l = (List) serviceRegistrations.get(ref.getID());
			if (l == null) {
				l = new ArrayList();
				serviceRegistrations.put(ref.getID(), l);
			}
			l.add(new RSEDAndSRAssoc(rsed, registration));
		}
	}

	RSEDAndSRAssoc[] removeServiceRegistration(IRemoteServiceReference reference) {
		getContainerAdapter().ungetRemoteService(reference);
		synchronized (serviceRegistrations) {
			List l = (List) serviceRegistrations.remove(reference.getID());
			if (l != null)
				return (RSEDAndSRAssoc[]) l.toArray(new RSEDAndSRAssoc[] {});
		}
		return null;
	}

	ServiceRegistration removeServiceRegistration(
			RemoteServiceEndpointDescription rsed) {
		if (rsed == null)
			return null;
		ServiceRegistration reg = null;
		synchronized (serviceRegistrations) {
			IRemoteServiceID remoteServiceID = null;
			for (Iterator i = serviceRegistrations.keySet().iterator(); i
					.hasNext();) {
				remoteServiceID = (IRemoteServiceID) i.next();
				List assocs = (List) serviceRegistrations.get(remoteServiceID);
				for (Iterator j = assocs.iterator(); j.hasNext();) {
					RSEDAndSRAssoc assoc = (RSEDAndSRAssoc) j.next();
					if (rsed.equals(assoc.getRSED())) {
						j.remove();
						reg = assoc.getSR();
					}
				}
			}
			if (reg != null && remoteServiceID != null) {
				serviceRegistrations.remove(remoteServiceID);
			}
		}
		return reg;
	}

	boolean hasRSED(RemoteServiceEndpointDescription rsed) {
		if (rsed == null)
			return false;
		synchronized (serviceRegistrations) {
			for (Iterator i = serviceRegistrations.keySet().iterator(); i
					.hasNext();) {
				List assocs = (List) serviceRegistrations.get(i.next());
				for (Iterator j = assocs.iterator(); j.hasNext();) {
					RSEDAndSRAssoc assoc = (RSEDAndSRAssoc) j.next();
					if (rsed.equals(assoc))
						return true;
				}
			}
		}
		return false;
	}

	boolean isEmpty() {
		synchronized (serviceRegistrations) {
			return serviceRegistrations.size() == 0;
		}
	}
}
