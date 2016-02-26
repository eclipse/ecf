/*******************************************************************************
* Copyright (c) 2016 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice;

import java.util.Dictionary;
import java.util.Map;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.equinox.concurrent.future.IExecutor;

/**
 * A container adapter intended for use by remote service host containers.  Implements IRemoteServiceContainerAdapter
 * and IRSAHostContainerAdapter.  A IRSAHostContainerAdapter that gets the actual registerService call
 * is expected to be provided upon construction.  
 * 
 * Subclasses should extend as appropriate.
 * 
 * @since 8.9
 */
public class RSARemoteServiceContainerAdapter extends RemoteServiceContainerAdapterImpl implements IRSAHostContainerAdapter {

	private final IRSAHostContainerAdapter rsaAdapter;

	public RSARemoteServiceContainerAdapter(IContainer container, IExecutor executor, IRSAHostContainerAdapter rsaAdapter) {
		super(container, executor);
		this.rsaAdapter = rsaAdapter;
	}

	public RSARemoteServiceContainerAdapter(IContainer container, IRSAHostContainerAdapter rsaAdapter) {
		super(container);
		this.rsaAdapter = rsaAdapter;
	}

	@Override
	protected RemoteServiceRegistrationImpl createRegistration() {
		return new RSARemoteServiceRegistration();
	}

	public class RSARemoteServiceRegistration extends RemoteServiceRegistrationImpl implements IExtendedRemoteServiceRegistration {
		private static final long serialVersionUID = 3245045338579222364L;

		private Map<String, Object> extraProperties;

		@Override
		public void publish(RemoteServiceRegistryImpl reg, Object svc, String[] clzzes, Dictionary props) {
			super.publish(reg, svc, clzzes, props);
			this.extraProperties = registerEndpoint(this);
		}

		public void unregister() {
			unregisterEndpoint(this);
		}

		public Map<String, Object> getExtraProperties() {
			return extraProperties;
		}
	}

	public Map<String, Object> registerEndpoint(RSARemoteServiceRegistration registration) {
		return (rsaAdapter != null) ? rsaAdapter.registerEndpoint(registration) : null;
	}

	public void unregisterEndpoint(RSARemoteServiceRegistration registration) {
		if (rsaAdapter != null)
			rsaAdapter.unregisterEndpoint(registration);
	}
}
