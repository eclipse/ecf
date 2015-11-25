/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.service.model;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

/**
 * @since 3.3
 */
public class AbstractServicesContentProvider extends BaseWorkbenchContentProvider {

	private IViewSite viewSite;
	private final ServicesRootNode invisibleRoot;

	public AbstractServicesContentProvider(IViewSite viewSite) {
		this.viewSite = viewSite;
		this.invisibleRoot = new ServicesRootNode(""); //$NON-NLS-1$
	}

	protected IViewSite getViewSite() {
		return this.viewSite;
	}

	protected ServicesRootNode getInvisibleRoot() {
		return this.invisibleRoot;
	}

	public Object[] getElements(Object parent) {
		if (parent.equals(viewSite)) {
			return getChildren(getInvisibleRoot());
		}
		return getChildren(parent);
	}

}
