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

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ImportRegistration implements
		org.osgi.service.remoteserviceadmin.ImportRegistration {

	private IRemoteServiceContainer rsContainer;
	private IRemoteServiceReference rsReference;
	private ServiceRegistration importRegistration;
	private ImportReference importReference;
	private Throwable throwable;
	private final Object closeLock = new Object();

	protected ImportRegistration(IRemoteServiceContainer rsContainer,
			IRemoteServiceReference rsReference,
			EndpointDescription endpointDescription,
			ServiceRegistration importRegistration) {
		this.rsContainer = rsContainer;
		this.rsReference = rsReference;
		this.importRegistration = importRegistration;
		this.importReference = new ImportReference(
				importRegistration.getReference(), endpointDescription);
	}

	public IRemoteServiceReference getRemoteServiceReference() {
		return rsReference;
	}

	public IRemoteServiceContainer getRemoteServiceContainer() {
		return rsContainer;
	}

	public ImportReference getImportReference() {
		synchronized (closeLock) {
			Throwable t = getException();
			if (t != null)
				throw new IllegalStateException(
						"Cannot get import reference as registration not properly initialized",
						t);
			return importReference;
		}
	}

	public void close() {
		synchronized (closeLock) {
			if (importRegistration != null) {
				importRegistration.unregister();
				importRegistration = null;
			}
			if (rsReference != null) {
				rsContainer.getContainerAdapter().ungetRemoteService(
						rsReference);
				rsReference = null;
				rsContainer = null;
			}
			if (importReference != null) {
				importReference.close();
				importReference = null;
			}
			throwable = null;
		}
	}

	public Throwable getException() {
		synchronized (closeLock) {
			return throwable;
		}
	}

	@Override
	public String toString() {
		return "ImportRegistration [rsReference=" + rsReference
				+ ", importReference=" + importReference + ", throwable="
				+ throwable + ", closeLock=" + closeLock + "]";
	}

}
