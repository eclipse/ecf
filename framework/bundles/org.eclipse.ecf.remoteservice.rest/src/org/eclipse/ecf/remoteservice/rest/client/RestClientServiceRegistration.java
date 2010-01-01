/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.client;

import java.util.Dictionary;
import java.util.Enumeration;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.internal.remoteservice.rest.RestClientServiceReference;
import org.eclipse.ecf.internal.remoteservice.rest.RestServiceRegistry;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;

/**
 * This class acts as the registration for {@link RestClientService}s.
 */
public class RestClientServiceRegistration implements IRemoteServiceRegistration {

	protected static final String CLASS_METHOD_SEPARATOR = "."; //$NON-NLS-1$
	protected String[] clazzes;
	protected IRemoteCallable[][] callables;
	protected IRemoteServiceReference reference;
	protected Dictionary properties;
	protected ID containerId;
	protected RestServiceRegistry registry;
	protected IRemoteServiceID serviceID;

	public RestClientServiceRegistration(String[] classNames, IRemoteCallable[][] restCalls, Dictionary properties, RestServiceRegistry registry) {
		Assert.isNotNull(classNames);
		this.clazzes = classNames;
		Assert.isNotNull(restCalls);
		Assert.isTrue(classNames.length == restCalls.length);
		this.callables = restCalls;
		this.properties = properties;
		containerId = registry.getContainerId();
		reference = new RestClientServiceReference(this);
		this.registry = registry;
		Namespace namespace = IDFactory.getDefault().getNamespaceByName(RestNamespace.NAME);
		this.serviceID = new RemoteServiceID(namespace, containerId, registry.getNextServiceId());
	}

	public RestClientServiceRegistration(IRemoteCallable[] restCalls, Dictionary properties, RestServiceRegistry registry) {
		Assert.isNotNull(restCalls);
		this.clazzes = new String[restCalls.length];
		for (int i = 0; i < restCalls.length; i++) {
			this.clazzes[i] = restCalls[i].getMethod();
		}
		this.callables = new IRemoteCallable[][] {restCalls};
		this.properties = properties;
		containerId = registry.getContainerId();
		reference = new RestClientServiceReference(this);
		this.registry = registry;
		Namespace namespace = IDFactory.getDefault().getNamespaceByName(RestNamespace.NAME);
		this.serviceID = new RemoteServiceID(namespace, containerId, registry.getNextServiceId());
	}

	public String[] getClazzes() {
		return clazzes;
	}

	public ID getContainerID() {
		return containerId;
	}

	public IRemoteServiceID getID() {
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
			if (element instanceof String) {
				result[i] = (String) element;
				i++;
			}
		}
		return result;
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

	protected RestClientContainer getRestClientContainer() {
		return registry.getContainer();
	}

	protected IRemoteCallable findDefaultRestCall(String methodToFind) {
		for (int i = 0; i < callables.length; i++) {
			String className = clazzes[i];
			IRemoteCallable[] subArray = callables[i];
			for (int j = 0; j < subArray.length; j++) {
				IRemoteCallable def = subArray[j];
				String defMethod = def.getMethod();
				String fqDefMethod = getFQMethod(className, defMethod);
				if (fqDefMethod.equals(methodToFind))
					return def;
			}
		}
		return null;
	}

	public static String getFQMethod(String className, String defMethod) {
		return className + CLASS_METHOD_SEPARATOR + defMethod;
	}

	protected IRemoteCallable findCallable(IRemoteCall remoteCall) {
		String callMethod = remoteCall.getMethod();
		if (callMethod == null)
			return null;
		IRemoteCallable defaultRestCall = null;
		for (int i = 0; i < clazzes.length; i++) {
			if (clazzes[i].equals(callMethod)) {
				// The method name given is the fully qualified name
				defaultRestCall = callables[i][0];
			}
		}
		return (defaultRestCall != null) ? defaultRestCall : findDefaultRestCall(callMethod);
	}

	protected IRemoteCallable lookupCallable(IRemoteCall remoteCall) {
		if (remoteCall == null)
			return null;
		return findCallable(remoteCall);
	}

}
