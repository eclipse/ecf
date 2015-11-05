/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa.model;

import org.eclipse.ecf.internal.remoteservices.ui.Messages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

/**
 * @since 3.3
 */
public class RSAContentProvider extends BaseWorkbenchContentProvider {

	private IViewSite viewSite;

	private ExportedServicesRootNode invisibleRoot;
	private ExportedServicesRootNode exportedServicesRoot;
	private ImportedEndpointsRootNode importedEndpointsRoot;

	public RSAContentProvider(IViewSite viewSite) {
		this.viewSite = viewSite;
	}

	public ExportedServicesRootNode getExportedServicesRoot() {
		return exportedServicesRoot;
	}

	public ImportedEndpointsRootNode getImportedEndpointsRoot() {
		return importedEndpointsRoot;
	}

	public Object[] getElements(Object parent) {
		if (parent.equals(viewSite)) {
			if (invisibleRoot == null) {
				invisibleRoot = new ExportedServicesRootNode(""); //$NON-NLS-1$
				exportedServicesRoot = new ExportedServicesRootNode(
						Messages.RSAContentProvider_ExportedServicesNodeName);
				invisibleRoot.addChild(exportedServicesRoot);
				importedEndpointsRoot = new ImportedEndpointsRootNode(
						Messages.RSAContentProvider_ImportedEndpointsNodeName);
				invisibleRoot.addChild(importedEndpointsRoot);
			}
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

}
