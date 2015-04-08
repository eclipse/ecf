/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.model;

import org.osgi.service.remoteserviceadmin.RemoteConstants;

public class EndpointFrameworkIDNode extends EndpointPropertyNode {

	public EndpointFrameworkIDNode() {
		super(RemoteConstants.ENDPOINT_FRAMEWORK_UUID);
		setPropertyAlias("Framework ID");
	}

	@Override
	public Object getPropertyValue() {
		return getEndpointDescription().getFrameworkUUID();
	}
}
