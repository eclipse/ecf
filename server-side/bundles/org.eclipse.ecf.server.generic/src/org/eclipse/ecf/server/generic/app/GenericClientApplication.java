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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.internal.server.generic.Activator;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * @since 3.0
 */
public class GenericClientApplication extends AbstractGenericClientApplication implements IApplication {

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
		return (ISharedObjectContainer) Activator.getDefault().getContainerManager().getContainerFactory().createContainer("ecf.generic.client"); //$NON-NLS-1$
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
		String[] originalArgs = (String[]) context.getArguments().get("application.args"); //$NON-NLS-1$
		if (originalArgs == null)
			return new String[0];
		final List l = new ArrayList();
		for (int i = 0; i < originalArgs.length; i++)
			if (!originalArgs[i].equals("-pdelaunch")) //$NON-NLS-1$
				l.add(originalArgs[i]);
		return (String[]) l.toArray(new String[] {});
	}

}
