/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.jmdns.container;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.identity.ServiceID;

public class JMDNSServiceInfo extends ServiceInfo {

	private static final long serialVersionUID = -5229813165370975600L;
	public static final String PROP_PROTOCOL_NAME = "protocol";
	public static final String PROP_PATH_NAME = "path";
	public static final String SLASH = "/";
	
	public JMDNSServiceInfo(InetAddress address, ServiceID id, int port, int priority, int weight, IServiceProperties props) {
		super(address, id, port, priority, weight, props);
	}
	
	public JMDNSServiceInfo(InetAddress address, String type, int port, int priority, int weight, IServiceProperties props) {
		super(address,type,port,priority,weight,props);
	}

	protected String getProtocolFromType(String type) {
		String res = type.trim();
		while (res.startsWith("_")) {
			res = res.substring(1);
		}
		int dotLoc = res.indexOf(".");
		if (dotLoc != -1) {
			res = res.substring(0,dotLoc);
		}
		return res;
	}
	protected URI getServiceURI0() throws URISyntaxException {
		IServiceProperties props = this.getServiceProperties();
		if (props == null) throw new URISyntaxException("invalid serviceinfo","no properties");
		String protocol = null;
		String path = null;
		try {
			protocol = (String) props.getPropertyString(PROP_PROTOCOL_NAME);
			path = (String) props.getPropertyString(PROP_PATH_NAME);
		} catch (Exception e) {
			throw new URISyntaxException("invalid serviceinfo properties",e.getMessage());
		}
		if (protocol == null || protocol.equals("")) {
			protocol = getProtocolFromType(getServiceID().getServiceType());
		}
		if (path == null || path.equals("")) {
			path = SLASH;
		} else if (!path.startsWith(SLASH)) {
			path = SLASH+path;
		}
		return new URI(protocol,null,getAddress().getHostAddress(),getPort(),path,null,null);
	}
	public URI getServiceURI() throws URISyntaxException {
		if (this.isResolved()) {
			return getServiceURI0();
		} else {
			throw new URISyntaxException("service info not resolved","");
		}
	}
}
