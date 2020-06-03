/****************************************************************************
 * Copyright (c) 2009 Composent and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.remoteservice.rest;

import java.net.URI;
import java.net.URL;
import java.util.Dictionary;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.client.IRemoteCallable;
import org.eclipse.ecf.remoteservice.client.IRemoteServiceClientContainerAdapter;
import org.eclipse.ecf.tests.ECFAbstractTestCase;

public abstract class AbstractRestTestCase extends ECFAbstractTestCase {

	protected IContainer createRestContainer(String uri) throws ContainerCreateException {
		return getContainerFactory().createContainer(
				RestConstants.REST_CONTAINER_TYPE, uri);
	}

	protected IContainer createRestContainer(ID restID) throws ContainerCreateException {
		return getContainerFactory().createContainer(
				RestConstants.REST_CONTAINER_TYPE, restID);
	}
	
	protected ID createRestID(String id) throws IDCreateException {
		return getIDFactory().createID(RestConstants.NAMESPACE, id);
	}
	
	protected ID createRestID(URL id) throws IDCreateException {
		return getIDFactory().createID(RestConstants.NAMESPACE, new Object[] { id });
	}

	protected ID createRestID(URI id) throws IDCreateException {
		return getIDFactory().createID(RestConstants.NAMESPACE, new Object[] { id });
	}

	protected IRemoteServiceClientContainerAdapter getRemoteServiceClientContainerAdapter(IContainer container) {
		return (IRemoteServiceClientContainerAdapter) container.getAdapter(IRemoteServiceClientContainerAdapter.class);
	}
	
	protected IRemoteServiceRegistration registerCallable(IContainer container, IRemoteCallable callable, Dictionary properties) {
		return getRemoteServiceClientContainerAdapter(container).registerCallables(new IRemoteCallable[] { callable }, properties);
	}

	protected IRemoteServiceRegistration registerCallable(IContainer container, IRemoteCallable[] callables, Dictionary properties) {
		return getRemoteServiceClientContainerAdapter(container).registerCallables(callables, properties);
	}
	
}
