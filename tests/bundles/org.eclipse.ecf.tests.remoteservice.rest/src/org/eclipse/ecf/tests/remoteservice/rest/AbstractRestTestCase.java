/*******************************************************************************
* Copyright (c) 2009 Composent and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.remoteservice.rest;

import java.net.URI;
import java.net.URL;
import java.util.Dictionary;
import java.util.List;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.rest.IRestCallable;
import org.eclipse.ecf.remoteservice.rest.client.IRestClientContainerAdapter;
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

	protected IRestClientContainerAdapter getRestClientContainerAdapter(IContainer container) {
		return (IRestClientContainerAdapter) container.getAdapter(IRestClientContainerAdapter.class);
	}
	
	protected IRemoteServiceRegistration registerCallable(IContainer container, IRestCallable callable, Dictionary properties) {
		return getRestClientContainerAdapter(container).registerCallable(callable, properties);
	}

	protected IRemoteServiceRegistration registerCallable(IContainer container, IRestCallable[] callables, Dictionary properties) {
		return getRestClientContainerAdapter(container).registerCallable(callables, properties);
	}
	
	protected IRemoteServiceRegistration registerCallable(IContainer container, Class clazz, List callables, Dictionary properties) {
		return getRestClientContainerAdapter(container).registerCallable(new Class[] { clazz} , callables, properties);
	}

}
