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

import org.eclipse.ecf.internal.remoteservices.ui.Messages;
import org.eclipse.ui.IViewSite;

/**
 * @since 3.3
 */
public class RSAContentProvider extends AbstractRSAContentProvider {

	private ExportedServicesRootNode exportedServicesRoot;
	private ImportedEndpointsRootNode importedEndpointsRoot;

	public RSAContentProvider(IViewSite viewSite) {
		super(viewSite);
		ExportedServicesRootNode invisibleRoot = getInvisibleRoot();
		exportedServicesRoot = new ExportedServicesRootNode(Messages.RSAContentProvider_ExportedServicesNodeName);
		invisibleRoot.addChild(exportedServicesRoot);
		importedEndpointsRoot = new ImportedEndpointsRootNode(Messages.RSAContentProvider_ImportedEndpointsNodeName);
		invisibleRoot.addChild(importedEndpointsRoot);
	}

	public ExportedServicesRootNode getExportedServicesRoot() {
		return exportedServicesRoot;
	}

	public ImportedEndpointsRootNode getImportedEndpointsRoot() {
		return importedEndpointsRoot;
	}

}
