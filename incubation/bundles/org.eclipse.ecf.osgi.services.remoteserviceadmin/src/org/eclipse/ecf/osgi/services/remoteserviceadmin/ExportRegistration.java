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
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceReference;

public class ExportRegistration implements
		org.osgi.service.remoteserviceadmin.ExportRegistration {

	private IRemoteServiceRegistration rsRegistration;
	private ExportReference exportReference;

	private ServiceReference serviceReference;
	private Throwable exception;

	public ExportRegistration(IRemoteServiceRegistration rsRegistration,
			ServiceReference proxyServiceReference,
			EndpointDescription endpointDescription) {
		Assert.isNotNull(rsRegistration);
		this.rsRegistration = rsRegistration;
		this.serviceReference = proxyServiceReference;
		this.exportReference = new ExportReference(proxyServiceReference,
				endpointDescription);
	}

	public ExportRegistration(ServiceReference serviceReference, Throwable t) {
		this.serviceReference = serviceReference;
		this.exception = t;
	}

	public synchronized org.osgi.service.remoteserviceadmin.ExportReference getExportReference() {
		Throwable t = getException();
		if (t != null)
			throw new IllegalStateException(
					"Cannot get export reference as registration not properly initialized",
					t);
		return exportReference;
	}

	public synchronized boolean matchesServiceReference(
			ServiceReference serviceReference) {
		if (serviceReference == null)
			return false;
		return (this.serviceReference.equals(serviceReference));
	}

	public synchronized IRemoteServiceRegistration getRemoteServiceRegistration() {
		return rsRegistration;
	}

	public synchronized void close() {
		if (rsRegistration != null) {
			rsRegistration.unregister();
			rsRegistration = null;
		}
		if (exportReference != null) {
			exportReference.close();
			exportReference = null;
		}
		exception = null;
	}

	public synchronized Throwable getException() {
		return exception;
	}

	public synchronized String toString() {
		return "ExportRegistration[rsRegistration=" + rsRegistration
				+ ", exportReference=" + exportReference
				+ ", serviceReference=" + serviceReference + ", exception="
				+ exception + "]";
	}

}
