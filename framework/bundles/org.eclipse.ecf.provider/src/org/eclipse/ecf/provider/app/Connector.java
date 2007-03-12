/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.app;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ecf.provider.generic.TCPServerSOContainer;


public class Connector {
	public static final int DEFAULT_PORT = TCPServerSOContainer.DEFAULT_PORT;
	public static final int DEFAULT_TIMEOUT = TCPServerSOContainer.DEFAULT_KEEPALIVE;
	public static final String DEFAULT_HOSTNAME = TCPServerSOContainer.DEFAULT_HOST;
	public static final String DEFAULT_SERVERNAME = TCPServerSOContainer.DEFAULT_NAME;
	public static final String DEFAULT_PROTOCOL = TCPServerSOContainer.DEFAULT_PROTOCOL;
	
	int port = DEFAULT_PORT;
	int timeout = DEFAULT_TIMEOUT;
	String protocol = DEFAULT_PROTOCOL;
	String hostname = DEFAULT_HOSTNAME;
	List groups = new ArrayList();
	
	public Connector(String protocol, String host, int port, int timeout) {
		if (protocol != null && !protocol.equals("")) this.protocol = protocol; //$NON-NLS-1$
		if (host != null && !host.equals("")) this.hostname = host; //$NON-NLS-1$
		else {
			try {
				InetAddress addr = InetAddress.getLocalHost();
				this.hostname = addr.getCanonicalHostName();
			} catch (Exception e) {
				this.hostname = "localhost"; //$NON-NLS-1$
			}
		}
		this.port = port;
		this.timeout = timeout;
	}
	public boolean addGroup(NamedGroup grp) {
		if (grp == null) return false;
		for(Iterator i=groups.iterator(); i.hasNext(); ) {
			NamedGroup namedGroup = (NamedGroup) i.next();
			if (namedGroup.getName().equals(grp.getName())) return false;
		}
		groups.add(grp);
		grp.setParent(this);
		return true;
	}
	public String getProtocol() {
		return protocol;
	}
	public String getHostname() {
		return hostname;
	}
	public int getPort() {
		return port;
	}
	public int getTimeout() {
		return timeout;
	}
	public List getGroups() {
		return groups;
	}
	public String getID() {
		return getProtocol()+"://"+getHostname()+":"+getPort(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}