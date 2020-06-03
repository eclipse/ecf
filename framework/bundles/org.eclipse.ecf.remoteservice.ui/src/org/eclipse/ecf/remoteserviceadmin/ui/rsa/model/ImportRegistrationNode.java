/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa.model;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportReference;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportRegistration;
import org.eclipse.ecf.remoteservices.ui.util.PropertyUtils;
import org.osgi.framework.ServiceReference;

/**
 * @since 3.3
 */
public class ImportRegistrationNode extends AbstractRegistrationNode {

	private final ImportRegistration importRegistration;

	/**
	 * @since 3.4
	 */
	public ImportRegistrationNode(Throwable t, boolean showStack) {
		super(t, showStack);
		this.importRegistration = null;
	}

	public ImportRegistrationNode(Throwable t) {
		this(t, false);
	}

	public ImportRegistrationNode(ImportRegistration iReg) {
		super(iReg.getException(), false);
		this.importRegistration = iReg;
	}

	@Override
	protected String getErrorName() {
		return "Import " + super.getErrorName();
	}

	protected ImportRegistration getImportRegistration() {
		return this.importRegistration;
	}

	protected ServiceReference getImportedService() {
		ImportReference iRef = getImportReference();
		return (iRef == null) ? null : iRef.getImportedService();
	}

	protected ImportReference getImportReference() {
		ImportRegistration iReg = getImportRegistration();
		return (iReg == null) ? null : (ImportReference) iReg.getImportReference();
	}

	public String getValidName() {
		return PropertyUtils.convertObjectClassToString(getImportedService());
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
