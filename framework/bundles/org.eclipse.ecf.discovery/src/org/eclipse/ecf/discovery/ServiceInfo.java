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
import java.util.Map;

import org.eclipse.ecf.core.identity.ServiceID;

public class ServiceInfo implements IServiceInfo, Serializable {

	private static final long serialVersionUID = 1L;

	InetAddress addr = null;

	ServiceID serviceID;

	int port;

	int priority;

	int weight;

	Map properties;

	public ServiceInfo(InetAddress address, ServiceID id, int port,
			int priority, int weight, Map props) {
		this.addr = address;
		this.serviceID = id;
		this.port = port;
		this.priority = priority;
		this.weight = weight;
		this.properties = props;
	}

	public InetAddress getAddress() {
		return addr;
	}

	protected void setAddress(InetAddress address) {
		this.addr = address;
	}

	public ServiceID getServiceID() {
		return serviceID;
	}

	public int getPort() {
		return port;
	}

	public int getPriority() {
		return priority;
	}

	public int getWeight() {
		return weight;
	}

	public Map getProperties() {
		return properties;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ServiceInfo[");
		buf.append("addr=").append(addr).append(";id=").append(serviceID).append(
				";port=").append(port).append(";priority=").append(priority)
				.append(";weight=").append(weight).append(";props=").append(
						properties).append("]");
		return buf.toString();
	}
}
