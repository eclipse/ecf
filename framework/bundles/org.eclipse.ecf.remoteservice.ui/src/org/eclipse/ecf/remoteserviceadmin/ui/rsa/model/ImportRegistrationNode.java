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

	public ImportRegistrationNode(Throwable t) {
		super(t);
		this.importRegistration = null;
	}

	protected ImportRegistration getImportRegistration() {
		return this.importRegistration;
	}
	
	protected ServiceReference getImportedService() {
		ImportReference iRef = getImportReference();
		return (iRef == null)?null:iRef.getImportedService();
	}
	
	protected ImportReference getImportReference() {
		ImportRegistration iReg = getImportRegistration();
		return (iReg == null) ? null
				: (ImportReference) iReg.getImportReference();
	}

	public String getValidName() {
		return convertObjectClassToString(getImportedService());
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
		if (importRegistration != null)
			importRegistration.close();
	}

}
