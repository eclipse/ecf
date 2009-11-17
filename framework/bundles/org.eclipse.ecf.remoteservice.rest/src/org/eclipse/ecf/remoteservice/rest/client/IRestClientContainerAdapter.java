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

import java.util.Dictionary;
import java.util.List;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.rest.IRestCallable;
import org.eclipse.ecf.remoteservice.rest.resource.IRestResourceProcessor;

public interface IRestClientContainerAdapter extends IRemoteServiceContainerAdapter {

	/**
	 * Register rest remote service.
	 * 
	 * @param callable the rest callable to register.  Must not be <code>null</code>.
	 * @param properties service properties to associate with the rest service.
	 * @return IRemoteServiceRegistration that allows the registered rest service to be accessed.  If
	 * <code>null</code> registration failed.
	 */
	public IRemoteServiceRegistration registerCallable(IRestCallable callable, Dictionary properties);

	public IRemoteServiceRegistration registerCallable(IRestCallable[] callables, Dictionary properties);

	public IRemoteServiceRegistration registerCallable(String[] clazzes, IRestCallable[][] callables, Dictionary properties);

	public IRemoteServiceRegistration registerCallable(Class[] clazzes, List callables, Dictionary properties);

	public IRemoteServiceRegistration registerCallable(String[] clazzes, List callables, Dictionary properties);

	// set/get rest resource
	public void setRestResource(IRestResourceProcessor resource);

	public IRestResourceProcessor getRestResource();

}
