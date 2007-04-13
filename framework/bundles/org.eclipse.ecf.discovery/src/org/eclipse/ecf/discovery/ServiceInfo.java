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
import java.net.InetAddress;

import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceID;

/**
 * Base implementation of {@link IServiceInfo}.  Subclasses
 * may be created as appropriate.
 */
public class ServiceInfo implements IServiceInfo, Serializable {

	private static final long serialVersionUID = 1L;

	InetAddress addr = null;

	IServiceID serviceID;

	int port;

	int priority;

	int weight;

	IServiceProperties properties;

	public ServiceInfo(InetAddress address, String type, int port,
			int priority, int weight, IServiceProperties props) {
		this.addr = address;
		this.serviceID = new ServiceID(type, null);
		this.port = port;
		this.priority = priority;
		this.weight = weight;
		this.properties = props;
	}

	public ServiceInfo(InetAddress address, IServiceID serviceID, int port,
			int priority, int weight, IServiceProperties props) {
		this.addr = address;
		this.serviceID = serviceID;
		this.port = port;
		this.priority = priority;
		this.weight = weight;
		this.properties = props;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getAddress()
	 */
	public InetAddress getAddress() {
		return addr;
	}

	protected void setAddress(InetAddress address) {
		this.addr = address;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getServiceID()
	 */
	public IServiceID getServiceID() {
		return serviceID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceInfo#getPort()
	 */
	public int getPort() {
		return port;
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
		return (addr != null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ServiceInfo["); //$NON-NLS-1$
		buf.append("addr=").append(addr).append(";id=").append(serviceID) //$NON-NLS-1$ //$NON-NLS-2$
				.append(";port=").append(port).append(";priority=").append( //$NON-NLS-1$ //$NON-NLS-2$
						priority).append(";weight=").append(weight).append( //$NON-NLS-1$
						";props=").append(properties).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

}
