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

import org.eclipse.ecf.internal.remoteservices.ui.OverlayIcon;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.remoteservices.ui.RSAImageRegistry;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 */
public class EndpointNodeWorkbenchAdapter extends AbstractEndpointNodeWorkbenchAdapter {

	/**
	 * @since 3.3
	 */
	protected ImageDescriptor edImageDesc;
	/**
	 * @since 3.3
	 */
	protected ImageDescriptor importedEndpointDesc;

	public EndpointNodeWorkbenchAdapter() {
		edImageDesc = RSAImageRegistry.ENDPOINTDESCRIPTION_OBJ;
		importedEndpointDesc = new OverlayIcon(edImageDesc,
				new ImageDescriptor[][] { { RSAImageRegistry.DESC_RSPROXY_CO } });
	}

	@Override
	public String getLabel(Object object) {
		EndpointDescription ed = ((EndpointNode) object).getEndpointDescription();
		return ed.getContainerID().getName() + ":" + ed.getRemoteServiceId(); //$NON-NLS-1$
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return ((EndpointNode) object).isImported() ? importedEndpointDesc : edImageDesc;
	}
}
