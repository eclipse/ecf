/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Composent, Inc - Simplifications
*******************************************************************************/
package org.eclipse.ecf.internal.remoteservice.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.rest.client.RestClientContainer;
import org.eclipse.ecf.remoteservice.rest.client.RestClientServiceRegistration;

/**
 * A registry for RestServices to fit the remote service API.
 */
public class RestServiceRegistry implements Serializable {

	private static final long serialVersionUID = -7002609161000008043L;
	private static long nextServiceId = 0L;
	private ID containerId;
	private List registrations;
	private RestClientContainer container;
	private IConnectContext connectContext;

	public RestServiceRegistry(RestClientContainer container) {
		Assert.isNotNull(container);
		this.containerId = container.getID();
		this.container = container;
		registrations = new ArrayList();
	}

	public RestClientContainer getContainer() {
		return container;
	}

	public long getNextServiceId() {
		return nextServiceId++;
	}

	public ID getContainerId() {
		return containerId;
	}

	public void registerRegistration(RestClientServiceRegistration registration) {
		if (!registrations.contains(registration))
			registrations.add(registration);
	}

	public void unregisterRegistration(RestClientServiceRegistration registration) {
		registrations.remove(registration);
	}

	public IRemoteServiceReference findServiceReference(IRemoteServiceID serviceID) {
		for (int i = 0; i < registrations.size(); i++) {
			IRemoteServiceRegistration reg = (IRemoteServiceRegistration) registrations.get(i);
			if (serviceID.equals(reg.getID()))
				return reg.getReference();
		}
		return null;
	}

	public RestClientServiceRegistration findServiceRegistration(RestClientServiceReference reference) {
		for (int i = 0; i < registrations.size(); i++) {
			RestClientServiceRegistration reg = (RestClientServiceRegistration) registrations.get(i);
			IRemoteServiceID serviceID = reference.getID();
			if (serviceID.equals(reg.getID()))
				return reg;
		}
		return null;
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, String clazz, IRemoteFilter remoteFilter) throws ContainerConnectException {
		if (target == null)
			return getRemoteServiceReferences((ID[]) null, clazz, remoteFilter);
		// If we're not already connected, then connect to targetID
		// If we *are* already connected, then we do *not* connect to target,
		// but rather just search for targetID/endpoint
		if (container.getConnectedID() == null) {
			container.connect(target, connectContext);
		}
		// Now we're connected (or already were connected), so we look for
		// remote service references for target
		return getRemoteServiceReferences(new ID[] {target}, clazz, remoteFilter);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter, String clazz, IRemoteFilter remoteFilter) {
		if (clazz == null)
			return null;
		List result = new ArrayList();
		for (int i = 0; i < registrations.size(); i++) {
			RestClientServiceRegistration reg = (RestClientServiceRegistration) registrations.get(i);
			if (idFilter == null || containsID(reg, idFilter)) {
				String[] clazzes = reg.getClazzes();
				boolean found = false;
				for (int j = 0; j < clazzes.length && !found; j++) {
					if (clazz.equals(clazzes[j]) && !result.contains(reg.getReference())) {
						result.add(reg.getReference());
						found = true;
					}
				}
			}
		}
		// check the filter
		if (remoteFilter != null) {
			for (int i = 0; i < result.size(); i++) {
				RestClientServiceReference ref = (RestClientServiceReference) result.get(i);
				if (!remoteFilter.match(ref))
					result.remove(i);
			}
		}
		if (result.size() > 0) {
			RestClientServiceReference[] array = new RestClientServiceReference[result.size()];
			result.toArray(array);
			return (array.length == 0) ? null : array;
		}
		return null;
	}

	private boolean containsID(RestClientServiceRegistration reg, ID[] idFilter) {
		for (int i = 0; i < idFilter.length; i++) {
			if (reg.getID().equals(idFilter[i]))
				return true;
		}
		return false;
	}

	public IRemoteServiceID getRemoteServiceID(ID containerID, long containerRelativeID) {
		if (containerID.equals(this.containerId)) {
			for (int i = 0; i < registrations.size(); i++) {
				RestClientServiceRegistration reg = (RestClientServiceRegistration) registrations.get(i);
				if (reg.getID().getContainerRelativeID() == containerRelativeID)
					return reg.getID();
			}
		}
		return null;
	}

	public String[] getClazzes(IRemoteServiceReference reference) {
		for (int i = 0; i < registrations.size(); i++) {
			RestClientServiceRegistration reg = (RestClientServiceRegistration) registrations.get(i);
			if (reg.getReference().equals(reference)) {
				return reg.getClazzes();
			}
		}
		return null;
	}

	public IRemoteServiceReference[] getAllRemoteServiceReferences(String clazz, IRemoteFilter remoteFilter) {
		List result = new ArrayList();
		for (int i = 0; i < registrations.size(); i++) {
			RestClientServiceRegistration reg = (RestClientServiceRegistration) registrations.get(i);
			String[] clazzes = reg.getClazzes();
			boolean found = false;
			for (int j = 0; j < clazzes.length && !found; j++) {
				if (clazz.equals(clazzes[j]) && !result.contains(reg.getReference())) {
					result.add(reg.getReference());
					found = true;
				}
			}
		}
		// check the filter
		if (remoteFilter != null) {
			for (int i = 0; i < result.size(); i++) {
				RestClientServiceReference ref = (RestClientServiceReference) result.get(i);
				if (!remoteFilter.match(ref))
					result.remove(i);
			}
		}
		if (result.size() > 0) {
			RestClientServiceReference[] array = new RestClientServiceReference[result.size()];
			result.toArray(array);
			return (array.length == 0) ? null : array;
		}
		return null;
	}

}
