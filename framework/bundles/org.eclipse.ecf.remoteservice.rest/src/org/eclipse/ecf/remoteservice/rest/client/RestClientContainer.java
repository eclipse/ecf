/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.rest.client;

import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.client.IRemoteServiceClientContainerAdapter;
import org.eclipse.ecf.remoteservice.client.RemoteServiceClientRegistration;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;

/**
 * A container for REST services. 
 */
public class RestClientContainer extends AbstractRestClientContainer implements IRemoteServiceClientContainerAdapter {

	public RestClientContainer(RestID id) {
		super(id);
	}

	protected IRemoteService createRemoteService(RemoteServiceClientRegistration registration) {
		return new RestClientService(this, registration);
	}

}
