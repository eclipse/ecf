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
import java.util.Hashtable;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.client.*;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;
import org.eclipse.ecf.remoteservice.util.RemoteFilterImpl;
import org.osgi.framework.InvalidSyntaxException;

/**
 * A container for REST services. 
 */
public class RestClientContainer extends AbstractClientContainer implements IRemoteServiceClientContainerAdapter {

	public RestClientContainer(RestID id) {
		super(id);
		// Set serializers
		setParameterSerializer(new StringParameterSerializer());
		setResponseDeserializer(new XMLRemoteResponseDeserializer());
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, ID[] idFilter, String clazz, String filter) throws InvalidSyntaxException, ContainerConnectException {
		return super.getRemoteServiceReferences(transformTarget(target, filter), idFilter, clazz, filter);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, String clazz, String filter) throws InvalidSyntaxException, ContainerConnectException {
		return super.getRemoteServiceReferences(transformTarget(target, filter), clazz, filter);
	}

	protected ID transformTarget(ID originalTarget, String filter) throws InvalidSyntaxException {
		if (originalTarget != null && filter != null && originalTarget instanceof RestID)
			((RestID) originalTarget).setRsId(new RemoteFilterImpl(filter).getRsId());
		return originalTarget;
	}

	protected class RestRemoteServiceClientRegistration extends RemoteServiceClientRegistration {

		public RestRemoteServiceClientRegistration(Namespace namespace, IRemoteCallable[] restCalls, Dictionary properties, RemoteServiceClientRegistry registry) {
			super(namespace, restCalls, properties, registry);
			ID cID = getConnectedID();
			if (cID != null)
				this.containerId = cID;
			long rsId = ((RestID) containerId).getRsId();
			this.serviceID = new RemoteServiceID(namespace, containerId, rsId);
			if (rsId > 0) {
				if (this.properties == null)
					this.properties = new Hashtable();
				this.properties.put(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID, new Long(rsId));
			}
		}

		public RestRemoteServiceClientRegistration(Namespace namespace, String[] classNames, IRemoteCallable[][] restCalls, Dictionary properties, RemoteServiceClientRegistry registry) {
			super(namespace, classNames, restCalls, properties, registry);
			ID cID = getConnectedID();
			if (cID != null)
				this.containerId = cID;
			long rsId = ((RestID) containerId).getRsId();
			this.serviceID = new RemoteServiceID(namespace, containerId, rsId);
			if (rsId > 0) {
				if (this.properties == null)
					this.properties = new Hashtable();
				this.properties.put(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID, new Long(rsId));
			}
		}
	}

	protected RemoteServiceClientRegistration createRestServiceRegistration(String[] clazzes, IRemoteCallable[][] callables, Dictionary properties) {
		return new RestRemoteServiceClientRegistration(getRemoteServiceNamespace(), clazzes, callables, properties, registry);
	}

	protected RemoteServiceClientRegistration createRestServiceRegistration(IRemoteCallable[] callables, Dictionary properties) {
		return new RestRemoteServiceClientRegistration(getRemoteServiceNamespace(), callables, properties, registry);
	}

	protected IRemoteService createRemoteService(RemoteServiceClientRegistration registration) {
		return new RestClientService(this, registration);
	}

	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(RestNamespace.NAME);
	}

	public String prepareEndpointAddress(IRemoteCall call, IRemoteCallable callable) {
		String resourcePath = callable.getResourcePath();
		if (resourcePath == null || "".equals(resourcePath)) //$NON-NLS-1$
			return null;
		// if resourcePath startswith http then we use it unmodified
		if (resourcePath.startsWith("http://")) //$NON-NLS-1$
			return resourcePath;

		RestID targetContainerID = (RestID) getRemoteCallTargetID();
		String baseUriString = targetContainerID.toURI().toString();
		int length = baseUriString.length();
		char[] lastChar = new char[1];
		baseUriString.getChars(length - 1, length, lastChar, 0);
		char[] firstMethodChar = new char[1];
		resourcePath.getChars(0, 1, firstMethodChar, 0);
		if ((lastChar[0] == '/' && firstMethodChar[0] != '/') || (lastChar[0] != '/' && firstMethodChar[0] == '/'))
			return baseUriString + resourcePath;
		else if (lastChar[0] == '/' && firstMethodChar[0] == '/') {
			String tempurl = baseUriString.substring(0, length - 1);
			return tempurl + resourcePath;
		} else if (lastChar[0] != '/' && firstMethodChar[0] != '/')
			return baseUriString + "/" + resourcePath; //$NON-NLS-1$
		return null;
	}

	public boolean setRemoteServiceCallPolicy(IRemoteServiceCallPolicy policy) {
		// REST clients don't have way to invoke call policy by default,
		// so return false
		return false;
	}

}
