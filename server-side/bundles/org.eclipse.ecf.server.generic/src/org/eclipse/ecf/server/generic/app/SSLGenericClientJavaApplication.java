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
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.provider.generic.SSLGenericContainerInstantiator;

/**
 * @since 6.0
 */
public class SSLGenericClientJavaApplication extends SSLAbstractGenericClientApplication {

	protected ISharedObjectContainer createContainer() throws ContainerCreateException {
		IContainerFactory containerFactory = ContainerFactory.getDefault();
		containerFactory.addDescription(new ContainerTypeDescription("ecf.generic.secure.client", new SSLGenericContainerInstantiator(), null)); //$NON-NLS-1$
		return (ISharedObjectContainer) containerFactory.createContainer("ecf.generic.secure.client"); //$NON-NLS-1$
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
