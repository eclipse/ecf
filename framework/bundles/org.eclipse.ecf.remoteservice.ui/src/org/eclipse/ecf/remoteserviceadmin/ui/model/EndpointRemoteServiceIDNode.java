/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.model;

import org.eclipse.ecf.remoteservice.Constants;

public class EndpointRemoteServiceIDNode extends EndpointECFNode {

	public EndpointRemoteServiceIDNode() {
		super(Constants.SERVICE_ID);
		setPropertyAlias("Remote Service Id");
	}

	@Override
	public Object getPropertyValue() {
		return getEndpointDescription().getRemoteServiceId();
	}
}
