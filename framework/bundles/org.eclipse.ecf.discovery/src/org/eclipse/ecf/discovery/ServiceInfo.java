/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery;

import java.io.Serializable;
import java.net.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.internal.discovery.DiscoveryPlugin;
import org.eclipse.ecf.internal.discovery.Messages;

/**
 * Base implementation of {@link IServiceInfo}. Subclasses may be created as
 * appropriate.
 */
public class ServiceInfo implements IServiceInfo, Serializable,
		IContainerServiceInfoAdapter {

	private static final long serialVersionUID = -5651115550295457142L;

	public static final int DEFAULT_PRIORITY = -1;
	public static final int DEFAULT_WEIGHT = -1;
	public static final String UNKNOWN_PROTOCOL = "unknown"; //$NON-NLS-1$

	protected URI uri = null;

	protected IServiceID serviceID;

	protected int priority;

	protected int weight;

	protected IServiceProperties properties;

	protected ServiceInfo() {
		// null constructor for subclasses
	}

	public ServiceInfo(URI anURI, IServiceID serviceID, int priority,
			int weight, IServiceProperties props) {
		this.uri = anURI;
		Assert.isNotNull(this.uri);
		this.serviceID = serviceID;
		Assert.isNotNull(serviceID);
		this.priority = priority;
		this.weight = weight;
		this.properties = (props == null) ? new ServiceProperties() : props;
	}

	public ServiceInfo(String protocol, String host, int port,
			IServiceID serviceID, int priority, int weight,
			IServiceProperties props) {
		try {
			if (host == null)
				host = InetAddress.getLocalHost().getHostAddress();
			uri = new URI((protocol == null) ? UNKNOWN_PROTOCOL : protocol,
					null, host, port, null, null, null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					Messages.ServiceInfo_EXCEPTION_INVALID_HOST_ARG);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException(
					Messages.ServiceInfo_EXCEPTION_NO_LOCALHOST);
		}
		this.serviceID = serviceID;
		Assert.isNotNull(serviceID);
		this.priority = priority;
		this.weight = weight;
		this.properties = (props == null) ? new ServiceProperties() : props;
	}

	public ServiceInfo(String protocol, String host, int port,
			IServiceID serviceID, IServiceProperties props) {
		this(protocol, host, port, serviceID, DEFAULT_PRIORITY, DEFAULT_WEIGHT,
				props);
	}

	public ServiceInfo(String protocol, String host, int port,
			IServiceID serviceID) {
		this(protocol, host, port, serviceID, new ServiceProperties());
	}

	public ServiceInfo(URI anURI, IServiceID serviceID, IServiceProperties props) {
		this(anURI, serviceID, DEFAULT_PRIORITY, DEFAULT_WEIGHT, props);
	}

	public ServiceInfo(URI anURI, IServiceID serviceID) {
		this(anURI, serviceID, DEFAULT_PRIORITY, DEFAULT_WEIGHT,
				new ServiceProperties());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getAddress()
	 */
	public URI getLocation() {
		return uri;
	}

	protected void setLocation(URI address) {
		this.uri = address;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getServiceID()
	 */
	public IServiceID getServiceID() {
		return serviceID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getPriority()
	 */
	public int getPriority() {
		return priority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getWeight()
	 */
	public int getWeight() {
		return weight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getServiceProperties()
	 */
	public IServiceProperties getServiceProperties() {
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceInfo#isResolved()
	 */
	public boolean isResolved() {
		return (uri != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer buf = new StringBuffer("ServiceInfo["); //$NON-NLS-1$
		buf.append("uri=").append(uri).append(";id=").append(serviceID) //$NON-NLS-1$ //$NON-NLS-2$
				.append(";priority=").append( //$NON-NLS-1$
						priority).append(";weight=").append(weight).append( //$NON-NLS-1$
						";props=").append(properties).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		}
		final IAdapterManager adapterManager = DiscoveryPlugin.getDefault()
				.getAdapterManager();
		if (adapterManager == null)
			return null;
		return adapterManager.loadAdapter(this, adapter.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ecf.discovery.IContainerServiceInfoAdapter#
	 * getContainerFactoryName()
	 */
	public String getContainerFactoryName() {
		return (properties == null) ? null
				: properties
						.getPropertyString(IContainerServiceInfoAdapter.CONTAINER_FACTORY_NAME_PROPERTY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IContainerServiceInfoAdapter#getTarget()
	 */
	public String getConnectTarget() {
		if (uri == null || properties == null)
			return null;
		String connectTarget = properties
				.getPropertyString(IContainerServiceInfoAdapter.CONTAINER_CONNECT_TARGET);
		if (connectTarget != null)
			return connectTarget;
		String t = properties
				.getPropertyString(IContainerServiceInfoAdapter.CONTAINER_CONNECT_TARGET_PROTOCOL);
		if (t == null)
			return null;
		StringBuffer target = new StringBuffer(t);
		String auth = uri.getAuthority();
		String path = properties
				.getPropertyString(IContainerServiceInfoAdapter.CONTAINER_CONNECT_TARGET_PATH);
		if (path == null)
			path = "/"; //$NON-NLS-1$
		target.append("://").append(auth).append("/").append(path); //$NON-NLS-1$ //$NON-NLS-2$
		return target.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.discovery.IContainerServiceInfoAdapter#setContainerProperties
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean)
	 */
	public void setContainerProperties(String containerFactoryName,
			String connectProtocol, String connectPath,
			Boolean connectRequiresPassword) {
		Assert.isNotNull(containerFactoryName);
		properties.setPropertyString(
				IContainerServiceInfoAdapter.CONTAINER_FACTORY_NAME_PROPERTY,
				containerFactoryName);
		Assert.isNotNull(connectProtocol);
		properties.setPropertyString(
				IContainerServiceInfoAdapter.CONTAINER_CONNECT_TARGET_PROTOCOL,
				connectProtocol);
		if (connectPath != null)
			properties.setPropertyString(
					IContainerServiceInfoAdapter.CONTAINER_CONNECT_TARGET_PATH,
					connectPath);
		if (connectRequiresPassword != null)
			properties
					.setPropertyString(
							IContainerServiceInfoAdapter.CONTAINER_CONNECT_REQUIRES_PASSWORD,
							connectRequiresPassword.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ecf.discovery.IContainerServiceInfoAdapter#
	 * connectRequiresPassword()
	 */
	public Boolean connectRequiresPassword() {
		String b = properties
				.getPropertyString(IContainerServiceInfoAdapter.CONTAINER_CONNECT_REQUIRES_PASSWORD);
		if (b == null)
			return null;
		return Boolean.valueOf(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.discovery.IContainerServiceInfoAdapter#setContainerProperties
	 * (java.lang.String, java.lang.String, java.lang.Boolean)
	 */
	public void setContainerProperties(String containerFactoryName,
			String connectTarget, Boolean connectRequiresPassword) {
		Assert.isNotNull(containerFactoryName);
		properties.setPropertyString(
				IContainerServiceInfoAdapter.CONTAINER_FACTORY_NAME_PROPERTY,
				containerFactoryName);
		Assert.isNotNull(connectTarget);
		properties.setPropertyString(
				IContainerServiceInfoAdapter.CONTAINER_CONNECT_TARGET,
				connectTarget);
		if (connectRequiresPassword != null)
			properties
					.setPropertyString(
							IContainerServiceInfoAdapter.CONTAINER_CONNECT_REQUIRES_PASSWORD,
							connectRequiresPassword.toString());
	}
}
