/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.core.runtime.Assert;
import org.osgi.framework.ServiceReference;

public class ExportReference implements
		org.osgi.service.remoteserviceadmin.ExportReference {

	private ServiceReference serviceReference;
	private EndpointDescription endpointDescription;
	
	protected ExportReference(ServiceReference serviceReference, EndpointDescription endpointDescription) {
		Assert.isNotNull(serviceReference);
		this.serviceReference = serviceReference;
		Assert.isNotNull(endpointDescription);
		this.endpointDescription = endpointDescription;
	}
	
	public synchronized ServiceReference getExportedService() {
		return serviceReference;
	}

	public synchronized org.osgi.service.remoteserviceadmin.EndpointDescription getExportedEndpoint() {
		return endpointDescription;
	}

	public synchronized void close() {
		this.serviceReference = null;
		this.endpointDescription = null;
	}
}
