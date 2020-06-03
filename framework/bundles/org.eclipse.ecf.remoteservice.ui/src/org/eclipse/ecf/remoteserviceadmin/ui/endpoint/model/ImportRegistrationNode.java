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
package org.eclipse.ecf.remoteserviceadmin.ui.endpoint.model;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportRegistration;

/**
 * @since 3.3
 */
public class ImportRegistrationNode extends AbstractEndpointNode {

	private final ImportRegistration importRegistration;

	public ImportRegistrationNode(ImportRegistration ir) {
		this.importRegistration = ir;
	}

	public ImportRegistration getImportRegistration() {
		return importRegistration;
	}

	@Override
	public String toString() {
		return "ImportRegistrationNode [importRegistration=" //$NON-NLS-1$
				+ importRegistration + "]"; //$NON-NLS-1$
	}

}
