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

import org.eclipse.ecf.remoteservices.ui.RSAImageRegistry;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @since 3.3
 */
public class NameValuePropertyNodeWorkbenchAdapter extends AbstractRSAWorkbenchAdapter {

	@Override
	public String getLabel(Object object) {
		NameValuePropertyNode nvpn = (NameValuePropertyNode) object;
		String propertyName = nvpn.getPropertyAlias();
		if (propertyName == null)
			propertyName = nvpn.getPropertyName();
		return propertyName + nvpn.getNameValueSeparator() + nvpn.getPropertyValue();
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return RSAImageRegistry.DESC_PROPERTY_OBJ;
	}
}
