/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.discovery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.internal.osgi.services.discovery.Activator;
import org.eclipse.ecf.internal.osgi.services.discovery.ServicePropertyUtils;

public abstract class RemoteServiceEndpointDescription implements
		IRemoteServiceEndpointDescription {

	protected Map serviceProperties;

	public RemoteServiceEndpointDescription(Map properties) {
		this.serviceProperties = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.discovery.ServiceEndpointDescription#getEndpointID()
	 */
	public String getEndpointID() {
		Object o = serviceProperties.get(ServicePublication.ENDPOINT_ID);
		if (o instanceof String) {
			return (String) o;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.osgi.service.discovery.ServiceEndpointDescription#
	 * getEndpointInterfaceName(java.lang.String)
	 */
	public String getEndpointInterfaceName(String interfaceName) {
		if (interfaceName == null)
			return null;
		Object o = serviceProperties
				.get(ServicePublication.ENDPOINT_INTERFACE_NAME);
		if (o == null || !(o instanceof String)) {
			return null;
		}
		String intfNames = (String) o;
		Collection c = ServicePropertyUtils
				.createCollectionFromString(intfNames);
		if (c == null)
			return null;
		for (Iterator i = c.iterator(); i.hasNext();) {
			String intfName = (String) i.next();
			if (intfName != null && intfName.startsWith(interfaceName)) {
				// return just endpointInterfaceName
				return intfName
						.substring(
								intfName.length()
										+ ServicePropertyUtils.ENDPOINT_INTERFACE_NAME_SEPARATOR
												.length()).trim();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.discovery.ServiceEndpointDescription#getLocation()
	 */
	public URI getLocation() {
		Object o = serviceProperties.get(ServicePublication.ENDPOINT_LOCATION);
		if (o == null || !(o instanceof String)) {
			return null;
		}
		String uriExternalForm = (String) o;
		URI uri = null;
		try {
			uri = new URI(uriExternalForm);
		} catch (URISyntaxException e) {
			Activator.getDefault()
					.log(
							new Status(IStatus.ERROR, Activator.PLUGIN_ID,
									IStatus.ERROR,
									"Exception getting location URI", e));//$NON-NLS-1$
		}
		return uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.discovery.ServiceEndpointDescription#getProperties()
	 */
	public Map getProperties() {
		return serviceProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.discovery.ServiceEndpointDescription#getProperty(java
	 * .lang.String)
	 */
	public Object getProperty(String key) {
		return serviceProperties.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.discovery.ServiceEndpointDescription#getPropertyKeys()
	 */
	public Collection getPropertyKeys() {
		return serviceProperties.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.discovery.ServiceEndpointDescription#getProvidedInterfaces
	 * ()
	 */
	public Collection getProvidedInterfaces() {
		Object o = serviceProperties
				.get(ServicePublication.SERVICE_INTERFACE_NAME);
		if (o == null || !(o instanceof String)) {
			throw new NullPointerException();
		}
		final String providedInterfacesStr = (String) o;
		return ServicePropertyUtils
				.createCollectionFromString(providedInterfacesStr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.discovery.ServiceEndpointDescription#getVersion(java
	 * .lang.String)
	 */
	public String getVersion(String interfaceName) {
		Collection c = getProvidedInterfaces();
		if (c == null) {
			return null;
		}
		for (Iterator i = c.iterator(); i.hasNext();) {
			String intfName = (String) i.next();
			if (intfName != null && intfName.startsWith(interfaceName)) {
				// return just version string
				return intfName
						.substring(
								intfName.length()
										+ ServicePropertyUtils.INTERFACE_VERSION_SEPARATOR
												.length()).trim();
			}
		}
		return null;
	}

	public long getRemoteServiceId() {
		byte[] remoteServiceIdAsBytes = (byte[]) serviceProperties
				.get(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		if (remoteServiceIdAsBytes == null)
			return 0;
		else {
			Long remoteServiceId = new Long(new String(remoteServiceIdAsBytes));
			return remoteServiceId.longValue();
		}
	}

	public abstract ID getEndpointAsID();

	public abstract ID getConnectTargetID();

	public abstract IServiceID getServiceID();

	public String getRemoteServicesFilter() {
		Object o = serviceProperties
				.get(RemoteServicePublication.REMOTE_SERVICE_FILTER);
		if (o instanceof String)
			return (String) o;
		return null;
	}

	public void setProperties(Map props) {
		if (props != null)
			this.serviceProperties = props;
	}

	public String[] getSupportedConfigs() {
		Object o = serviceProperties
				.get(RemoteServicePublication.ENDPOINT_SUPPORTED_CONFIGS);
		if (o == null || !(o instanceof String))
			return null;
		Collection c = ServicePropertyUtils
				.createCollectionFromString((String) o);
		return (String[]) ((c == null) ? null : c.toArray(new String[] {}));
	}

	public String[] getServiceIntents() {
		Object o = serviceProperties
				.get(RemoteServicePublication.ENDPOINT_SERVICE_INTENTS);
		if (o == null || !(o instanceof String))
			return null;
		Collection c = ServicePropertyUtils
				.createCollectionFromString((String) o);
		return (String[]) ((c == null) ? null : c.toArray(new String[] {}));
	}
}
