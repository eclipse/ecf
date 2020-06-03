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

/**
 * @since 3.3
 */
public class EndpointPropertyNodeWorkbenchAdapter extends AbstractEndpointNodeWorkbenchAdapter {

	@Override
	public String getLabel(Object object) {
		EndpointPropertyNode epn = (EndpointPropertyNode) object;
		String propertyName = epn.getPropertyAlias();
		if (propertyName == null)
			propertyName = epn.getPropertyName();
		return propertyName + epn.getNameValueSeparator() + epn.getPropertyValue();
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return RSAImageRegistry.DESC_PROPERTY_OBJ;
	}
}
