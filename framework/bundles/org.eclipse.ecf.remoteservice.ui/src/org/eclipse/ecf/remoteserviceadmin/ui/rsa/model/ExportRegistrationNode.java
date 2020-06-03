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

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ExportReference;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ExportRegistration;
import org.eclipse.ecf.remoteservices.ui.util.PropertyUtils;
import org.osgi.framework.ServiceReference;

/**
 * @since 3.3
 */
public class ExportRegistrationNode extends AbstractRegistrationNode {

	private final ExportRegistration exportRegistration;

	/**
	 * @since 3.4
	 */
	public ExportRegistrationNode(Throwable t, boolean showStack) {
		super(t, showStack);
		this.exportRegistration = null;
	}

	public ExportRegistrationNode(Throwable t) {
		this(t, false);
	}

	public ExportRegistrationNode(ExportRegistration eReg) {
		super(eReg.getException(), false);
		this.exportRegistration = eReg;
	}

	@Override
	protected String getErrorName() {
		return "Export " + super.getErrorName();
	}

	protected ExportRegistration getExportRegistration() {
		return this.exportRegistration;
	}

	protected ExportReference getExportReference() {
		ExportRegistration eReg = getExportRegistration();
		return eReg == null ? null : (ExportReference) eReg.getExportReference();
	}

	protected ServiceReference getExportedService() {
		ExportReference eRef = getExportReference();
		return eRef == null ? null : eRef.getExportedService();
	}

	public String getValidName() {
		return PropertyUtils.convertObjectClassToString(getExportedService());
	}

	@Override
	public boolean isClosed() {
		return getExportReference() == null;
	}

	@Override
	public ServiceReference getServiceReference() {
		ExportReference exportRef = getExportReference();
		return exportRef == null ? null : exportRef.getExportedService();
	}

	@Override
	public void close() {
		if (exportRegistration != null)
			exportRegistration.close();
	}
}
