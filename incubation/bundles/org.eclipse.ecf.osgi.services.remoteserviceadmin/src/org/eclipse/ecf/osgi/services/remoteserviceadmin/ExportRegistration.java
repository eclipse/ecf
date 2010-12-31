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

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceReference;

public class ExportRegistration implements
		org.osgi.service.remoteserviceadmin.ExportRegistration {

	// these members are set whether this ExportRegistration
	// is valid or not
	private ServiceReference serviceReference;
	private ID containerID;
	// This is only null if this ExportRegistration is invalid
	private ExportEndpoint exportEndpoint;
	// This is only non-null if this ExportRegistration is invalid
	private Throwable exception;

	public ExportRegistration(ExportEndpoint exportEndpoint) {
		Assert.isNotNull(exportEndpoint);
		this.exportEndpoint = exportEndpoint;
		this.serviceReference = exportEndpoint.getServiceReference();
		this.containerID = exportEndpoint.getContainerID();
		// Add ourselves to this exported endpoint
		this.exportEndpoint.add(this);
	}

	public ExportRegistration(ServiceReference serviceReference,
			ID containerID, Throwable t) {
		this.serviceReference = serviceReference;
		this.containerID = containerID;
		this.exception = t;
	}

	public ID getContainerID() {
		return containerID;
	}

	public synchronized org.osgi.service.remoteserviceadmin.ExportReference getExportReference() {
		Throwable t = getException();
		if (t != null)
			throw new IllegalStateException(
					"Cannot get export reference as export registration is invalid",
					t);
		return (exportEndpoint == null) ? null : exportEndpoint
				.getExportReference();
	}

	synchronized boolean match(ServiceReference serviceReference) {
		return match(serviceReference, null);
	}

	synchronized boolean match(ServiceReference serviceReference, ID containerID) {
		boolean containerIDMatch = (containerID == null) ? true
				: this.containerID.equals(containerID);
		return containerIDMatch
				&& this.serviceReference.equals(serviceReference);
	}

	synchronized ExportEndpoint getExportEndpoint(
			ServiceReference serviceReference, ID containerID) {
		return match(serviceReference, containerID) ? exportEndpoint : null;
	}

	synchronized IRemoteServiceRegistration getRemoteServiceRegistration() {
		return (exportEndpoint == null) ? null : exportEndpoint
				.getRemoteServiceRegistration();
	}

	public synchronized void close() {
		if (exportEndpoint != null) {
			exportEndpoint.close(this);
			exportEndpoint = null;
		}
		exception = null;
	}

	public synchronized Throwable getException() {
		return exception;
	}

	public synchronized String toString() {
		return "ExportRegistration[containerID=" + containerID
				+ ", serviceReference=" + serviceReference
				+ ", exportEndpoint=" + exportEndpoint + ", exception="
				+ exception + "]";
	}

}
