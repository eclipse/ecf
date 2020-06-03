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

import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;

/**
 * @since 3.3
 */
public class EndpointAsyncInterfacesNode extends EndpointECFNode {

	public EndpointAsyncInterfacesNode() {
		super(RemoteConstants.SERVICE_EXPORTED_ASYNC_INTERFACES);
	}

	@Override
	public Object getPropertyValue() {
		return getEndpointDescription().getAsyncInterfaces();
	}
}
