/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.server.generic.app;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.provider.generic.TCPServerSOContainer;

/**
 * @since 3.0
 */
public abstract class AbstractGenericClientApplication {

	protected String connectTarget;
	protected ISharedObjectContainer clientContainer;
	protected int waitTime = 40000;
	/**
	 * @since 5.1
	 */
	protected String clientId = null;
	/**
	 * @since 5.1
	 */
	protected String password = null;

	protected abstract ISharedObjectContainer createContainer() throws ContainerCreateException;

	protected void processArguments(String[] args) {
		connectTarget = TCPServerSOContainer.getDefaultServerURL();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-connectTarget")) { //$NON-NLS-1$
				connectTarget = args[i + 1];
				i++;
			}
			if (args[i].equals("-waitTime")) { //$NON-NLS-1$
				waitTime = new Integer(args[i + 1]).intValue();
				i++;
			}
			if (args[i].equals("-clientId")) { //$NON-NLS-1$
				clientId = args[i + 1];
			}
			if (args[i].equals("-connectPassword")) { //$NON-NLS-1$
				password = args[i + 1];
			}
		}
	}

	protected void initialize() throws ContainerCreateException {
		clientContainer = createContainer();
	}

	protected void connect() throws ContainerConnectException {
		clientContainer.connect(IDFactory.getDefault().createStringID(connectTarget), null);
	}

	protected void dispose() {
		if (clientContainer != null) {
			clientContainer.dispose();
			clientContainer = null;
		}
	}
}
