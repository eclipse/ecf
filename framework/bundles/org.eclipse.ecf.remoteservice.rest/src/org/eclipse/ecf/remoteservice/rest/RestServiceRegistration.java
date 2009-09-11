/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.remoteservice.rest;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.remoteservice.rest.RestServiceRegistry;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;

/**
 * This class acts as the registration for {@link RestService}s.
 */
public class RestServiceRegistration implements IRemoteServiceRegistration {

	private IRemoteServiceReference reference;
	private Object service;
	private String[] clazzes;
	private Dictionary properties;
	private ID containerId;
	private RestServiceRegistry registry;
	private IRemoteServiceID serviceID;

	public RestServiceRegistration(String[] clazzes, Object service, Dictionary properties, RestServiceRegistry registry) {
		this.service = service;
		this.clazzes = clazzes;
		this.properties = properties;
		containerId = registry.getContainerId();
		reference = new RestServiceReference(this);
		this.registry = registry;
		registry.registerRegistration(this);
	}
	
	public String[] getClazzes() {
		return clazzes;
	}

	public ID getContainerID() {
		return containerId;
	}

	public IRemoteServiceID getID() {
		if(serviceID == null){
			Namespace namespace = IDFactory.getDefault().getNamespaceByName(RestNamespace.NAME);
			URL baseURL = ((RestID)containerId).getBaseURL();
			serviceID = (IRemoteServiceID) IDFactory.getDefault().createID(namespace, new Object[] {baseURL, containerId, new Long(registry.getNextServiceId())});
		}
		return serviceID;
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public String[] getPropertyKeys() {
		int length = properties.size();
		Enumeration keys = properties.keys();
		String[] result = new String[length];
		int i = 0;
		while (keys.hasMoreElements()) {
			Object element = keys.nextElement();
			if(element instanceof String){
				result[i] = (String) element;
				i++;
			}
		}
		return result;
	}
	
	public IRemoteService getService() {
		return (IRemoteService)service;
	}

	public IRemoteServiceReference getReference() {	
		return reference;
	}

	public void setProperties(Dictionary properties) {
		this.properties = properties;
	}

	public void unregister() {
		registry.unregisterRegistration(this);
	}

	RestContainer getContainer() {
		return registry.getContainer();
	}

}
