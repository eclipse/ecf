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
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ImportRegistration implements
		org.osgi.service.remoteserviceadmin.ImportRegistration {

	private IRemoteServiceContainer rsContainer;
	private IRemoteServiceListener rsListener;
	private IRemoteServiceReference rsReference;
	private ServiceRegistration importRegistration;
	private ImportReference importReference;
	private Throwable throwable;
	private final Object closeLock = new Object();

	protected ImportRegistration(IRemoteServiceContainer rsContainer,
			IRemoteServiceListener rsListener,
			IRemoteServiceReference rsReference,
			EndpointDescription endpointDescription,
			ServiceRegistration importRegistration) {
		this.rsContainer = rsContainer;
		Assert.isNotNull(rsContainer);
		this.rsListener = rsListener;
		Assert.isNotNull(rsListener);
		this.rsReference = rsReference;
		Assert.isNotNull(rsReference);
		this.importRegistration = importRegistration;
		this.importReference = new ImportReference(
				importRegistration.getReference(), endpointDescription);
		// Add the remoteservice listener to the container adapter, so that the rsListener
		// notified asynchronously if our underlying remote service reference is unregistered locally
		// due to disconnect or remote ejection
		rsContainer.getContainerAdapter().addRemoteServiceListener(rsListener);
	}

	public IRemoteServiceReference getRemoteServiceReference() {
		synchronized (closeLock) {
			return rsReference;
		}
	}

	public IRemoteServiceContainer getRemoteServiceContainer() {
		synchronized (closeLock) {
			return rsContainer;
		}
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
			if (rsContainer != null) {
				IRemoteServiceContainerAdapter containerAdapter = rsContainer.getContainerAdapter();
				if (rsReference != null) containerAdapter.ungetRemoteService(rsReference);
				rsReference = null;
				// remove remote service listener
				if (rsListener != null) containerAdapter.removeRemoteServiceListener(rsListener);
				rsListener = null;
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
