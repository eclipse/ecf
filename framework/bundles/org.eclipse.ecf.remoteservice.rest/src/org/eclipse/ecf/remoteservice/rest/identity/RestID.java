/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.remoteservice.rest.identity;

import java.net.URL;

import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.rest.RestContainer;

/**
 * An ECF ID to instatiate a {@link RestContainer}.
 */
public class RestID extends BaseID implements IRemoteServiceID {
	
	private static final long serialVersionUID = -7725015677223101132L;
	URL baseUrl;
	private Long serviceID;
	private ID containerId;

	/**
	 * Contructor to create a RestID with a {@link Namespace}.
	 * 
	 * @param namespace Must be an instance of {@link RestNamespace}.
	 * @param baseURL an URL which will be associated with this ID to call REST services,
	 * i.e. http://twitter.com for Twitter services.
	 */
	public RestID( Namespace namespace, URL baseURL ) {
		super(namespace);
		this.baseUrl = baseURL;
	}

	/**
	 * @see RestID#RestID(Namespace, URL).
	 * 
	 * @param serviceID the service ID to use.
	 */
	public RestID(RestNamespace restNamespace, URL url, Long serviceID) {
		this(restNamespace, url);
		this.serviceID = serviceID;
	}

	/**
	 * @see RestID#RestID(RestNamespace, URL, Long).
	 * 
	 * @param containerId the ID of the associated container.
	 */
	public RestID(RestNamespace restNamespace, URL url, ID containerId, Long serviceID) {
		this(restNamespace, url, serviceID);
		this.containerId = containerId;
	}

	public int namespaceCompareTo(BaseID o) {
		return this.baseUrl.toExternalForm().compareTo(((RestID) o).toExternalForm());
	}

	public boolean namespaceEquals(BaseID o) {
		return this.baseUrl.equals(((RestID) o).baseUrl);
	}

	public String namespaceGetName() {
		return this.baseUrl.toExternalForm();
	}

	public int namespaceHashCode() {
		return this.baseUrl.hashCode();
	}
	
	public URL getBaseURL() {
		return baseUrl;
	}

	public ID getContainerID() {
		return containerId;
	}

	public long getContainerRelativeID() {
		if(serviceID == null)
			return 0;
		return serviceID.longValue();
	}
	
	public void setBaseUrl(URL baseUrl) {
		this.baseUrl = baseUrl;
	}

}
