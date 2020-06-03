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

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

/**
 * @since 3.3
 */
public class EndpointContentProvider extends BaseWorkbenchContentProvider {

	private IViewSite viewSite;
	private String topNodeLabel;

	private EndpointGroupNode invisibleRoot;
	private EndpointGroupNode root;

	public EndpointContentProvider(IViewSite viewSite, String topNodeLabel) {
		this.viewSite = viewSite;
		this.topNodeLabel = topNodeLabel;
	}

	public EndpointGroupNode getRootNode() {
		return root;
	}

	public Object[] getElements(Object parent) {
		if (parent.equals(viewSite)) {
			if (invisibleRoot == null) {
				invisibleRoot = new EndpointGroupNode(""); //$NON-NLS-1$
				root = new EndpointGroupNode(topNodeLabel);
				invisibleRoot.addChild(root);
			}
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

}
