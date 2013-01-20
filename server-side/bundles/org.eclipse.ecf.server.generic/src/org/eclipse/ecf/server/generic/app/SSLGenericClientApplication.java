/*******************************************************************************
* Copyright (c) 2013 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.server.generic.app;

import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.security.IConnectInitiatorPolicy;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.internal.server.generic.Activator;
import org.eclipse.ecf.provider.generic.SSLClientSOContainer;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * @since 6.0
 */
public class SSLGenericClientApplication extends SSLAbstractGenericClientApplication implements IApplication {

	private static final String GENERIC_CLIENT_CONTAINER_TYPE = "ecf.generic.secure.client"; //$NON-NLS-1$

	protected final Object appLock = new Object();
	protected boolean done = false;

	public Object start(IApplicationContext context) throws Exception {
		String[] args = getArguments(context);
		processArguments(args);

		initialize();

		connect();

		waitForDone();

		return IApplication.EXIT_OK;
	}

	public void stop() {
		dispose();
		synchronized (appLock) {
			done = true;
			appLock.notifyAll();
		}
	}

	protected ISharedObjectContainer createContainer() throws ContainerCreateException {
		IContainerFactory f = Activator.getDefault().getContainerManager().getContainerFactory();
		SSLClientSOContainer client = (SSLClientSOContainer) ((clientId == null) ? f.createContainer(GENERIC_CLIENT_CONTAINER_TYPE) : f.createContainer(GENERIC_CLIENT_CONTAINER_TYPE, clientId));
		if (password != null) {
			client.setConnectInitiatorPolicy(new IConnectInitiatorPolicy() {
				public void refresh() {
					//nothing
				}

				public Object createConnectData(IContainer container, ID targetID, IConnectContext context) {
					return password;
				}

				public int getConnectTimeout() {
					return 30000;
				}
			});
		}
		return client;
	}

	protected void waitForDone() {
		// then just wait here
		synchronized (appLock) {
			while (!done) {
				try {
					appLock.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	protected String[] getArguments(IApplicationContext context) {
		return (String[]) context.getArguments().get("application.args"); //$NON-NLS-1$
	}

}
