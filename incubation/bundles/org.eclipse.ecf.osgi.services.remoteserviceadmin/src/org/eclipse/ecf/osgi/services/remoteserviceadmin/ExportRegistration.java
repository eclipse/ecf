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
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceReference;

public class ExportRegistration implements
		org.osgi.service.remoteserviceadmin.ExportRegistration {

	private IRemoteServiceRegistration rsRegistration;	
	private ExportReference exportReference;
	private Throwable throwable;
	
	private final Object closeLock = new Object();
	
	public ExportRegistration(IRemoteServiceRegistration rsRegistration, ServiceReference serviceReference, EndpointDescription endpointDescription) {
		Assert.isNotNull(rsRegistration);
		this.rsRegistration = rsRegistration;
		this.exportReference = new ExportReference(serviceReference,endpointDescription);
	}
	
	public ExportRegistration(Throwable t) {
		this.throwable = t;
	}
	
	public org.osgi.service.remoteserviceadmin.ExportReference getExportReference() {
		synchronized (closeLock) {
			Throwable t = getException();
			if (t != null) throw new IllegalStateException("Cannot get export reference as registration not properly initialized",t);
			return exportReference;
		}
	}

	public void close() {
		synchronized (closeLock) {
			if (rsRegistration != null) {
				rsRegistration.unregister();
				rsRegistration = null;
			}
			exportReference = null;
			throwable = null;
		}
	}

	public Throwable getException() {
		synchronized (closeLock) {
			return throwable;
		}
	}

}
