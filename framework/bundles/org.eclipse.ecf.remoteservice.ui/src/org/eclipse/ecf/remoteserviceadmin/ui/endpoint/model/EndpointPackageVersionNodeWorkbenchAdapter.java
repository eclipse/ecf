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

import org.eclipse.ecf.remoteservices.ui.RSAImageRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Version;

/**
 * @since 3.3
 */
public class EndpointPackageVersionNodeWorkbenchAdapter extends AbstractEndpointNodeWorkbenchAdapter {

	@Override
	public String getLabel(Object object) {
		EndpointPackageVersionNode pvn = (EndpointPackageVersionNode) object;
		Version v = (Version) pvn.getPropertyValue();
		if (v == null)
			v = Version.emptyVersion;
		return pvn.getPropertyName() + " " + pvn.getPropertyAlias() //$NON-NLS-1$
				+ pvn.getNameValueSeparator() + v.toString();
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return RSAImageRegistry.DESC_PACKAGE_OBJ;
	}
}
