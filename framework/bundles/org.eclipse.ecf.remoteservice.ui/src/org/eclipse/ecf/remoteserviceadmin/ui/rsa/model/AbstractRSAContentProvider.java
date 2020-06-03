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

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

/**
 * @since 3.3
 */
public class AbstractRSAContentProvider extends BaseWorkbenchContentProvider {

	private IViewSite viewSite;
	private final ExportedServicesRootNode invisibleRoot;

	public AbstractRSAContentProvider(IViewSite viewSite) {
		this.viewSite = viewSite;
		this.invisibleRoot = new ExportedServicesRootNode(""); //$NON-NLS-1$
	}

	protected IViewSite getViewSite() {
		return this.viewSite;
	}

	protected ExportedServicesRootNode getInvisibleRoot() {
		return this.invisibleRoot;
	}

	public Object[] getElements(Object parent) {
		if (parent.equals(viewSite)) {
			return getChildren(getInvisibleRoot());
		}
		return getChildren(parent);
	}

}
