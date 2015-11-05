/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa.model;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportReference;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportRegistration;
import org.osgi.framework.ServiceReference;

/**
 * @since 3.3
 */
public class ImportRegistrationNode extends AbstractRegistrationNode {

	private final ImportRegistration importRegistration;

	public ImportRegistrationNode(ImportRegistration iReg) {
		super(iReg.getException());
		this.importRegistration = iReg;
	}

	ImportReference getImportReference() {
		return (ImportReference) this.importRegistration.getImportReference();
	}

	public String getValidName() {
		return convertObjectClassToString(getImportReference().getImportedService());
	}

	@Override
	public boolean isClosed() {
		return getImportReference() == null;
	}

	@Override
	public ServiceReference getServiceReference() {
		ImportReference importRef = getImportReference();
		return importRef == null ? null : importRef.getImportedService();
	}

	@Override
	public void close() {
		importRegistration.close();
	}

}
