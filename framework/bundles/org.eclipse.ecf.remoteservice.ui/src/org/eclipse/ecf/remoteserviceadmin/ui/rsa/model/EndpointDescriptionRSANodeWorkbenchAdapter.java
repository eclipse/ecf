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

import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.remoteserviceadmin.ui.endpoint.model.EndpointNodeWorkbenchAdapter;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 */
public class EndpointDescriptionRSANodeWorkbenchAdapter extends EndpointNodeWorkbenchAdapter {

	@Override
	public String getLabel(Object object) {
		EndpointDescription ed = ((EndpointDescriptionRSANode) object).getEndpointNode().getEndpointDescription();
		return ed.getContainerID().getName() + ":" + ed.getRemoteServiceId(); //$NON-NLS-1$
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return ((EndpointDescriptionRSANode) object).getEndpointNode().isImported() ? importedEndpointDesc
				: edImageDesc;
	}

	@Override
	public Object getParent(Object object) {
		return ((AbstractRSANode) object).getParent();
	}

	@Override
	public Object[] getChildren(Object object) {
		return ((EndpointDescriptionRSANode) object).getEndpointNode().getChildren();
	}

}
