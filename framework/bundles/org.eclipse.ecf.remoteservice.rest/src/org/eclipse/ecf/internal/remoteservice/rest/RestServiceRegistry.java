/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.internal.remoteservice.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.provider.remoteservice.generic.RemoteFilterImpl;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.rest.RestContainer;
import org.eclipse.ecf.remoteservice.rest.RestServiceReference;
import org.eclipse.ecf.remoteservice.rest.RestServiceRegistration;
import org.osgi.framework.InvalidSyntaxException;

/**
 * A registry for RestServices to fit the remote service API.
 */
public class RestServiceRegistry implements Serializable {

	private static final long serialVersionUID = -7002609161000008043L;
	private static long nextServiceId = 0L;
	private ID containerId;
	private List registrations;
	private RestContainer container;
	private IConnectContext connectContext;
	
	public RestServiceRegistry(RestContainer container) {
		this.containerId = container.getID();
		this.container = container;
		registrations = new ArrayList();
	}
	
	public RestContainer getContainer() {
		return container;
	}
	
	public long getNextServiceId() {
		return nextServiceId++;
	}
	
	public ID getContainerId() {
		return containerId;
	}
	
	public void registerRegistration(RestServiceRegistration registration) {
		if(!registrations.contains(registration))
			registrations.add(registration);
	}
	
	public void unregisterRegistration(RestServiceRegistration registration) {
		registrations.remove(registration);
	}

	public IRemoteService findService(IRemoteServiceReference reference) {
		for (int i = 0; i < registrations.size(); i++) {
			RestServiceRegistration reg = (RestServiceRegistration) registrations.get(i);
			if( reg.getReference().equals(reference))
				return reg.getService();
		}
		return null;		
	}

	public IRemoteServiceReference findServiceReference(IRemoteServiceID serviceID) {
		for (int i = 0; i < registrations.size(); i++) {
			IRemoteServiceRegistration reg = (IRemoteServiceRegistration)registrations.get(i);
			if( serviceID.equals(reg.getID()))
				return reg.getReference();
		}
		return null;
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, String clazz, String filter) {
		if (target == null)
			return getRemoteServiceReferences((ID[]) null, clazz, filter);
		// If we're not already connected, then connect to targetID
		// If we *are* already connected, then we do *not* connect to target, but rather just search for targetID/endpoint
		if (container.getConnectedID() == null) {
			try {
				container.connect(target, connectContext);
			} catch (ContainerConnectException e) {
				e.printStackTrace();
			}
		}
		// Now we're connected (or already were connected), so we look for remote service references for target
		return getRemoteServiceReferences(new ID[] {target}, clazz, filter);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter, String clazz, String filter) {
		if (clazz == null)
			return null;
		List result = new ArrayList();
		IRemoteFilter remoteFilter = null;
		try {
			remoteFilter = (filter == null) ? null : new RemoteFilterImpl(filter);
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < registrations.size(); i++) {
			RestServiceRegistration reg = (RestServiceRegistration)registrations.get(i);
			if(idFilter == null || containsID( reg, idFilter)) {
				String[] clazzes = reg.getClazzes();
				boolean found = false;
				for (int j = 0; j < clazzes.length && !found; j++) {
					if(clazz.equals(clazzes[j]) && !result.contains(reg.getReference())) {
						result.add(reg.getReference());
						found = true;
					}
				}
			}
		}
		// check the filter
		if(remoteFilter != null){
			for (int i = 0; i < result.size(); i++) {
				RestServiceReference ref = (RestServiceReference)result.get(i);
				if(!remoteFilter.match(ref))
					result.remove(i);
			}
		}
		if(result.size() > 0 ) {
			RestServiceReference[] array = new RestServiceReference[result.size()];
			result.toArray(array);
			return (array.length == 0) ? null : array;
		}
		return null;
	}

	private boolean containsID(RestServiceRegistration reg, ID[] idFilter) {
		for (int i = 0; i < idFilter.length; i++) {
			if(reg.getID().equals(idFilter[i]))
				return true;
		}
		return false;
	}

	public IRemoteServiceID getRemoteServiceID(ID containerID, long containerRelativeID) {
		if(containerID.equals(this.containerId)) {
			for (int i = 0; i < registrations.size(); i++) {
				RestServiceRegistration reg = (RestServiceRegistration) registrations.get(i);
				if(reg.getID().getContainerRelativeID() == containerRelativeID)
					return reg.getID();
			}
		}
		return null;
	}

	public String[] getClazzes(IRemoteServiceReference reference) {
		for (int i = 0; i < registrations.size(); i++) {
			RestServiceRegistration reg = (RestServiceRegistration) registrations.get(i);
			if(reg.getReference().equals(reference)) {
				return reg.getClazzes();
			}
		}
		return null;
	}

}
