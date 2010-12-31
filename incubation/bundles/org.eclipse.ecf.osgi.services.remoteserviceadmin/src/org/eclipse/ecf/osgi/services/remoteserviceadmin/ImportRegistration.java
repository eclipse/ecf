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
import org.eclipse.ecf.remoteservice.IRemoteServiceID;

public class ImportRegistration implements
		org.osgi.service.remoteserviceadmin.ImportRegistration {

	private ImportEndpoint importEndpoint;

	private ID containerID;
	private Throwable throwable;

	public ImportRegistration(ImportEndpoint importEndpoint) {
		Assert.isNotNull(importEndpoint);
		this.importEndpoint = importEndpoint;
		this.importEndpoint.add(this);
	}

	public ImportRegistration(ID containerID, Throwable t) {
		this.containerID = containerID;
		this.throwable = t;
	}

	public ID getContainerID() {
		return containerID;
	}

	synchronized boolean match(IRemoteServiceID remoteServiceID) {
		if (importEndpoint == null)
			return false;
		return importEndpoint.match(remoteServiceID);
	}

	public synchronized ImportReference getImportReference() {
		Throwable t = getException();
		if (t != null)
			throw new IllegalStateException(
					"Cannot get import reference as registration not properly initialized",
					t);
		return importEndpoint == null ? null : importEndpoint
				.getImportReference();
	}

	public synchronized void close() {
		if (importEndpoint != null) {
			importEndpoint.close(this);
			importEndpoint = null;
		}
		throwable = null;
	}

	public synchronized Throwable getException() {
		return throwable;
	}

	public synchronized String toString() {
		return "ImportRegistration [containerID=" + containerID
				+ ", importEndpoint=" + importEndpoint + ", exception="
				+ throwable + "]";
	}

}
