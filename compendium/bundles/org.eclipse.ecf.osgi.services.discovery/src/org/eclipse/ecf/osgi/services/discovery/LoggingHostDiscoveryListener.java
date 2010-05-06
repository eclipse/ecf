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
import org.osgi.framework.ServiceReference;

public class LoggingHostDiscoveryListener extends AbstractDiscoveryListener
		implements IHostDiscoveryListener {

	public LoggingHostDiscoveryListener(int logLevel) {
		super(logLevel);
	}

	public LoggingHostDiscoveryListener() {
	}

	public void publish(ServiceReference publicationServiceReference,
			IServiceInfo serviceInfo) {
		if (publicationServiceReference == null || serviceInfo == null)
			return;
		StringBuffer sb = new StringBuffer(
				"OSGi ECF service discovery: publish").append("\n");
		sb.append(createTabs(1)).append("serviceReference=")
				.append(publicationServiceReference).append("\n")
				.append(printServiceInfo(1, serviceInfo));
		log(publicationServiceReference, sb.toString(), null);
	}

	public void unpublish(ServiceReference publicationServiceReference,
			IServiceInfo serviceInfo) {
		if (publicationServiceReference == null || serviceInfo == null)
			return;
		StringBuffer sb = new StringBuffer(
				"OSGi ECF service discovery: unpublish").append("\n");
		sb.append(createTabs(1)).append("serviceReference=")
				.append(publicationServiceReference).append("\n")
				.append(printServiceInfo(1, serviceInfo));
		log(publicationServiceReference, sb.toString(), null);
	}

}
