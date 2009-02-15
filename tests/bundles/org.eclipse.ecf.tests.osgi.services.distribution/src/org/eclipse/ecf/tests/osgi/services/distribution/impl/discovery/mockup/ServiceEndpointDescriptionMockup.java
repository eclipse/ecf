/****************************************************************************
 * Copyright (c) 2009 Jan S. Rellermeyer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jan S. Rellermeyer - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution.impl.discovery.mockup;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.osgi.framework.Version;
import org.osgi.service.discovery.ServiceEndpointDescription;

public final class ServiceEndpointDescriptionMockup implements
		ServiceEndpointDescription {

	private ID serviceId;
	private String[] serviceInterfaces;
	private Map serviceProperties;

	public ServiceEndpointDescriptionMockup(final ID id,
			final String[] ifaces, final Map properties) {
		this.serviceId = id;
		this.serviceInterfaces = ifaces;
		this.serviceProperties = properties;
	}

	/**
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getEndpointID()
	 */
	public String getEndpointID() {
		return serviceId.toExternalForm();
	}

	/**
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getEndpointInterfaceName(java.lang.String)
	 */
	public String getEndpointInterfaceName(final String interfaceName) {
		return null;
	}

	/**
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getLocation()
	 */
	public URL getLocation() {
		return null;
	}

	/**
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getProperties()
	 */
	public Map getProperties() {
		return serviceProperties;
	}

	/**
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getProperty(java.lang.String)
	 */
	public Object getProperty(final String key) {
		return serviceProperties.get(key);
	}

	/**
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getPropertyKeys()
	 */
	public Collection getPropertyKeys() {
		return serviceProperties.keySet();
	}

	/**
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getProvidedInterfaces()
	 */
	public Collection getProvidedInterfaces() {
		return Arrays.asList(serviceInterfaces);
	}

	/**
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getVersion(java.lang.String)
	 */
	public String getVersion(final String interfaceName) {
		return Version.emptyVersion.toString();
	}

}
