/*******************************************************************************
 * Copyright (c) 2016 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis <slewis@composent.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservice;

import java.util.Map;
import org.eclipse.ecf.core.AbstractContainer;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.remoteservice.RSARemoteServiceContainerAdapter.RSARemoteServiceRegistration;

/**
 * Abstract container that is intended for use by RSA distribution providers.  Subclasses may extend
 * and override to create custom container adapter types.  By default, an instance of RSARemoteServiceContainerAdapter
 * is created by this class upon construction.
 * 
 * @since 8.9
 */
public abstract class AbstractRSAContainer extends AbstractContainer {

	private final ID id;
	private final RSARemoteServiceContainerAdapter containerAdapter;

	public AbstractRSAContainer(ID id) {
		this.id = id;
		this.containerAdapter = createContainerAdapter();
	}

	protected abstract Map<String, Object> registerEndpoint(RSARemoteServiceRegistration registration);

	protected abstract void unregisterEndpoint(RSARemoteServiceRegistration registration);

	protected RSARemoteServiceContainerAdapter createContainerAdapter() {
		return new RSARemoteServiceContainerAdapter(this, new IRSAHostContainerAdapter() {
			public void unregisterEndpoint(RSARemoteServiceRegistration registration) {
				AbstractRSAContainer.this.unregisterEndpoint(registration);
			}

			public Map<String, Object> registerEndpoint(RSARemoteServiceRegistration registration) {
				return AbstractRSAContainer.this.registerEndpoint(registration);
			}
		});
	}

	public void connect(ID targetID, IConnectContext connectContext) throws ContainerConnectException {
		throw new ContainerConnectException("Cannot connect this container"); //$NON-NLS-1$
	}

	public ID getConnectedID() {
		return null;
	}

	public Namespace getConnectNamespace() {
		return getID().getNamespace();
	}

	public void disconnect() {
		// do nothing
	}

	public ID getID() {
		return id;
	}

	@Override
	public Object getAdapter(Class serviceType) {
		Object result = super.getAdapter(serviceType);
		if (result == null && serviceType.isAssignableFrom(IRemoteServiceContainerAdapter.class))
			return containerAdapter;
		return null;
	}
}
