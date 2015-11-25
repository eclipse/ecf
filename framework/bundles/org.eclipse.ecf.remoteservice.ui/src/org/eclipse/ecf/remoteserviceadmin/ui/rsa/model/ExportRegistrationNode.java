/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
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

	public ExportRegistrationNode(ExportRegistration eReg) {
		super(eReg.getException());
		this.exportRegistration = eReg;
	}

	public ExportRegistrationNode(Throwable t) {
		super(t);
		this.exportRegistration = null;
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
