/*******************************************************************************
* Copyright (c) 2009 Composent and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.client;

import org.eclipse.ecf.remoteservice.IRemoteServiceClientContainerAdapter;
import org.eclipse.ecf.remoteservice.rest.resource.IRestResourceProcessor;

public interface IRestClientContainerAdapter extends IRemoteServiceClientContainerAdapter {

	// set/get rest resource
	public void setResourceProcessor(IRestResourceProcessor resource);

	public IRestResourceProcessor getResourceProcessor();

}
