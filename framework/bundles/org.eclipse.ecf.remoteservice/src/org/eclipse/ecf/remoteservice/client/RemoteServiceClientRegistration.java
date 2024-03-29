/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import java.util.Dictionary;
import java.util.Enumeration;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.*;

/**
 * Registrations for {@link AbstractClientContainer}.
 * 
 * @since 4.0
 */
public class RemoteServiceClientRegistration implements IRemoteServiceRegistration {

	protected static final String CLASS_METHOD_SEPARATOR = "."; //$NON-NLS-1$
	protected String[] clazzes;
	protected IRemoteCallable[][] callables;
	protected IRemoteServiceReference reference;
	protected Dictionary properties;
	protected ID containerId;
	protected RemoteServiceClientRegistry registry;
	protected IRemoteServiceID serviceID;

	public RemoteServiceClientRegistration(Namespace namespace, String[] classNames, IRemoteCallable[][] restCalls, Dictionary properties, RemoteServiceClientRegistry registry) {
		Assert.isNotNull(classNames);
		this.clazzes = classNames;
		Assert.isNotNull(restCalls);
		this.callables = restCalls;
		this.properties = properties;
		containerId = registry.getContainerId();
		reference = new RemoteServiceClientReference(this);
		this.registry = registry;
		this.serviceID = new RemoteServiceID(namespace, containerId, registry.getNextServiceId());
	}

	public RemoteServiceClientRegistration(Namespace namespace, IRemoteCallable[] restCalls, Dictionary properties, RemoteServiceClientRegistry registry) {
		Assert.isNotNull(restCalls);
		this.clazzes = new String[restCalls.length];
		for (int i = 0; i < restCalls.length; i++) {
			this.clazzes[i] = restCalls[i].getMethod();
		}
		this.callables = new IRemoteCallable[][] {restCalls};
		this.properties = properties;
		containerId = registry.getContainerId();
		reference = new RemoteServiceClientReference(this);
		this.registry = registry;
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
		if (properties == null)
			return null;
		return properties.get(key);
	}

	public String[] getPropertyKeys() {
		if (properties == null)
			return new String[] {};

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
		this.classLoader = null;
	}

	protected IRemoteCallable findDefaultRemoteCallable(String methodToFind) {
		for (int i = 0; i < callables.length; i++) {
			String className = clazzes[i];
			IRemoteCallable[] subArray = callables[i];
			for (int j = 0; j < subArray.length; j++) {
				IRemoteCallable def = subArray[j];
				String defMethod = def.getMethod();
				if (defMethod != null && defMethod.equals(methodToFind))
					return def;
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

	protected IRemoteCallable findRemoteCallable(IRemoteCall remoteCall) {
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
		return (defaultRestCall != null) ? defaultRestCall : findDefaultRemoteCallable(callMethod);
	}

	public IRemoteCallable lookupCallable(IRemoteCall remoteCall) {
		if (remoteCall == null)
			return null;
		return findRemoteCallable(remoteCall);
	}

	private ClassLoader classLoader = null;

	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	/**
	 * @since 8.14
	 */
	protected void setClassLoader(ClassLoader cl) {
		this.classLoader = cl;
	}
}
