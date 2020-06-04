/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server.generic.app;

import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.provider.generic.SSLGenericContainerInstantiator;

/**
 * @since 6.0
 */
public class SSLGenericClientJavaApplication extends AbstractGenericClientApplication {

	protected static final String GENERIC_SSL_CLIENT = "ecf.generic.ssl.client"; //$NON-NLS-1$

	protected ISharedObjectContainer createContainer() throws ContainerCreateException {
		IContainerFactory containerFactory = ContainerFactory.getDefault();
		containerFactory.addDescription(new ContainerTypeDescription(GENERIC_SSL_CLIENT, new SSLGenericContainerInstantiator(), null));
		return (ISharedObjectContainer) containerFactory.createContainer(GENERIC_SSL_CLIENT);
	}

	public static void main(String[] args) throws Exception {
		SSLGenericClientJavaApplication app = new SSLGenericClientJavaApplication();
		app.processArguments(args);
		app.initialize();
		app.connect();
		// wait for waitTime
		try {
			synchronized (app) {
				app.wait(app.waitTime);
			}
		} catch (InterruptedException e) {
			// nothing
		}
		app.dispose();
	}

}
