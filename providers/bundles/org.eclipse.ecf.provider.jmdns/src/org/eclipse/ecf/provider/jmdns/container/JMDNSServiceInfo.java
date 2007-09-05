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

import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.discovery.identity.ServiceID;

public class JMDNSServiceInfo extends ServiceInfo {

	private static final long serialVersionUID = -5229813165370975600L;
	public static final String PROP_PROTOCOL_NAME = "protocol"; //$NON-NLS-1$
	public static final String PROP_PATH_NAME = "path"; //$NON-NLS-1$
	public static final String SLASH = "/"; //$NON-NLS-1$

	public JMDNSServiceInfo(InetAddress address, ServiceID id, int port, int priority, int weight, IServiceProperties props) {
		super(address, id, port, priority, weight, props);
	}

}
