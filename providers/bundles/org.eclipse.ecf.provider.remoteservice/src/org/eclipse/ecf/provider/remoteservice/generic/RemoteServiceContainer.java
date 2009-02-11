/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.util.Dictionary;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.generic.TCPClientSOContainer;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.osgi.framework.InvalidSyntaxException;

public class RemoteServiceContainer extends TCPClientSOContainer implements IRemoteServiceContainerAdapter {

	protected IRemoteServiceContainerAdapter registry;

	protected void createRegistry() {
		registry = new RegistrySharedObject();
	}

	public RemoteServiceContainer(ISharedObjectContainerConfig config) {
		super(config);
		createRegistry();
	}

	public RemoteServiceContainer(ISharedObjectContainerConfig config, int ka) {
		super(config, ka);
		createRegistry();
	}

	public void addRemoteServiceListener(IRemoteServiceListener listener) {
		registry.addRemoteServiceListener(listener);
	}

	public IRemoteService getRemoteService(IRemoteServiceReference ref) {
		return registry.getRemoteService(ref);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter, String clazz, String filter) throws InvalidSyntaxException {
		return registry.getRemoteServiceReferences(idFilter, clazz, filter);
	}

	public IRemoteServiceRegistration registerRemoteService(String[] clazzes, Object service, Dictionary properties) {
		return registry.registerRemoteService(clazzes, service, properties);
	}

	public void removeRemoteServiceListener(IRemoteServiceListener listener) {
		registry.removeRemoteServiceListener(listener);
	}

	public boolean ungetRemoteService(IRemoteServiceReference ref) {
		return registry.ungetRemoteService(ref);
	}

	/**
	 * @since 3.0
	 */
	public IFuture asyncGetRemoteServiceReferences(ID[] idFilter, String clazz, String filter) {
		return registry.asyncGetRemoteServiceReferences(idFilter, clazz, filter);
	}

	/**
	 * @since 3.0
	 */
	public Namespace getRemoteServiceNamespace() {
		return getConnectNamespace();
	}

	/**
	 * @since 3.0
	 */
	public IRemoteFilter createRemoteFilter(String filter) throws InvalidSyntaxException {
		return registry.createRemoteFilter(filter);
	}
}
