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

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceRegistration;

public class ImportEndpoint {

	private IRemoteServiceContainerAdapter rsContainerAdapter;
	private IRemoteServiceListener rsListener;
	private IRemoteServiceReference rsReference;
	private ServiceRegistration proxyRegistration;
	private ImportReference importReference;
	private Set<ImportRegistration> importRegistrations;

	public ImportEndpoint(IRemoteServiceContainerAdapter rsContainerAdapter,
			IRemoteServiceReference rsReference,
			IRemoteServiceListener rsListener,
			ServiceRegistration proxyRegistration,
			EndpointDescription endpointDescription) {
		this.rsContainerAdapter = rsContainerAdapter;
		this.rsReference = rsReference;
		this.rsListener = rsListener;
		this.proxyRegistration = proxyRegistration;
		this.importReference = new ImportReference(
				proxyRegistration.getReference(), endpointDescription);
		// Add the remoteservice listener to the container adapter, so that the
		// rsListener
		// notified asynchronously if our underlying remote service reference is
		// unregistered locally
		// due to disconnect or remote ejection
		this.rsContainerAdapter.addRemoteServiceListener(this.rsListener);
		this.importRegistrations = new HashSet<ImportRegistration>();
	}

	synchronized ID getContainerID() {
		return (rsReference == null) ? null : rsReference.getContainerID();
	}

	synchronized boolean add(ImportRegistration importRegistration) {
		return this.importRegistrations.add(importRegistration);
	}

	synchronized boolean close(ImportRegistration importRegistration) {
		boolean removed = this.importRegistrations.remove(importRegistration);
		if (removed && importRegistrations.size() == 0) {
			if (proxyRegistration != null) {
				proxyRegistration.unregister();
				proxyRegistration = null;
			}
			if (rsContainerAdapter != null) {
				if (rsReference != null) {
					rsContainerAdapter.ungetRemoteService(rsReference);
					rsReference = null;
				}
				// remove remote service listener
				if (rsListener != null) {
					rsContainerAdapter.removeRemoteServiceListener(rsListener);
					rsListener = null;
				}
				rsContainerAdapter = null;
			}
			if (importReference != null) {
				importReference.close();
				importReference = null;
			}
		}
		return removed;
	}

	synchronized ImportReference getImportReference() {
		return importReference;
	}

	synchronized boolean match(IRemoteServiceID remoteServiceID) {
		if (remoteServiceID == null || rsReference == null)
			return false;
		return rsReference.getID().equals(remoteServiceID);
	}

	public synchronized String toString() {
		return "ImportEndpoint [rsReference=" + rsReference
				+ ", proxyRegistration=" + proxyRegistration
				+ ", importReference=" + importReference + "]";
	}

	synchronized boolean match(EndpointDescription ed) {
		if (importReference == null)
			return false;
		EndpointDescription importedEndpoint = (EndpointDescription) importReference
				.getImportedEndpoint();
		if (importedEndpoint == null)
			return false;
		return importedEndpoint.isSameService(ed);
	}

}
