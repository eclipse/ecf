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
import java.net.URI;
import org.eclipse.ecf.discovery.identity.IServiceID;

/**
 * Base implementation of {@link IServiceInfo}.  Subclasses
 * may be created as appropriate.
 */
public class ServiceInfo implements IServiceInfo, Serializable {

	private static final long serialVersionUID = -5651115550295457142L;

	public static final int DEFAULT_PRIORITY = 0;
	public static final int DEFAULT_WEIGHT = 0;

	protected URI uri = null;

	protected IServiceID serviceID;

	protected int priority;

	protected int weight;

	protected IServiceProperties properties;

	public ServiceInfo() {
		// null constructor for subclasses
	}

	public ServiceInfo(URI anURI, IServiceID serviceID, int priority, int weight, IServiceProperties props) {
		this.uri = anURI;
		this.serviceID = serviceID;
		this.priority = priority;
		this.weight = weight;
		this.properties = props;
	}

	public ServiceInfo(URI anURI, IServiceID serviceID, IServiceProperties props) {
		this(anURI, serviceID, DEFAULT_PRIORITY, DEFAULT_WEIGHT, props);
	}

	public ServiceInfo(URI anURI, IServiceID serviceID) {
		this(anURI, serviceID, DEFAULT_PRIORITY, DEFAULT_WEIGHT, new ServiceProperties());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getAddress()
	 */
	public URI getLocation() {
		return uri;
	}

	protected void setLocation(URI address) {
		this.uri = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getServiceID()
	 */
	public IServiceID getServiceID() {
		return serviceID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getPriority()
	 */
	public int getPriority() {
		return priority;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getWeight()
	 */
	public int getWeight() {
		return weight;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getServiceProperties()
	 */
	public IServiceProperties getServiceProperties() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#isResolved()
	 */
	public boolean isResolved() {
		return (uri != null);
	}

	/* (non-Javadoc)
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

}
