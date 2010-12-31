/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceReference;

public class ExportEndpoint {

	private ServiceReference serviceReference;
	private ID containerID;
	private IRemoteServiceRegistration rsRegistration;
	private ExportReference exportReference;
	private Set<ExportRegistration> exportRegistrations;

	public ExportEndpoint(ServiceReference serviceReference, ID containerID,
			IRemoteServiceRegistration reg,
			EndpointDescription endpointDescription) {
		Assert.isNotNull(serviceReference);
		Assert.isNotNull(reg);
		Assert.isNotNull(endpointDescription);
		this.serviceReference = serviceReference;
		this.containerID = containerID;
		this.rsRegistration = reg;
		this.exportReference = new ExportReference(serviceReference,
				endpointDescription);
		this.exportRegistrations = new HashSet<ExportRegistration>();
	}

	ID getContainerID() {
		return containerID;
	}

	ServiceReference getServiceReference() {
		return serviceReference;
	}

	synchronized ExportReference getExportReference() {
		return exportReference;
	}

	synchronized IRemoteServiceRegistration getRemoteServiceRegistration() {
		return rsRegistration;
	}

	synchronized boolean add(ExportRegistration exportRegistration) {
		return this.exportRegistrations.add(exportRegistration);
	}

	synchronized boolean close(ExportRegistration exportRegistration) {
		boolean removed = this.exportRegistrations.remove(exportRegistration);
		if (removed && exportRegistrations.size() == 0) {
			if (rsRegistration != null) {
				rsRegistration.unregister();
				rsRegistration = null;
			}
			if (exportReference != null) {
				exportReference.close();
				exportReference = null;
			}
		}
		return removed;
	}

	public synchronized String toString() {
		return "ExportEndpoint [rsRegistration=" + rsRegistration
				+ ", exportReference=" + exportReference + "]";
	}

}
