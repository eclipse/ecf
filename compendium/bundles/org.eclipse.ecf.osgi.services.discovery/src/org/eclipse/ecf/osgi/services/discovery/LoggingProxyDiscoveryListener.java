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

import org.eclipse.ecf.discovery.IServiceInfo;

public class LoggingProxyDiscoveryListener extends AbstractDiscoveryListener
		implements IProxyDiscoveryListener {

	public void discovered(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return;
		StringBuffer sb = new StringBuffer(
				"OSGi ECF service discovery: remote service discovered")
				.append("\n");
		sb.append(printServiceInfo(1, serviceInfo));
		log(null, sb.toString(), null);
	}

	public void undiscovered(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return;
		StringBuffer sb = new StringBuffer(
				"OSGi ECF service discovery: remote service undiscovered")
				.append("\n");
		sb.append(printServiceInfo(1, serviceInfo));
		log(null, sb.toString(), null);
	}

}
