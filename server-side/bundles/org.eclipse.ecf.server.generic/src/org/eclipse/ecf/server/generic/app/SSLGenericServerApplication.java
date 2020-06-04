/****************************************************************************
 * Copyright (c) 2013 Composent, Inc. and others.
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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * @since 6.0
 */
public class SSLGenericServerApplication extends SSLAbstractGenericServerApplication implements IApplication {

	protected final Object appLock = new Object();
	protected boolean done = false;

	public Object start(IApplicationContext context) throws Exception {
		String[] args = getArguments(context);
		processArguments(args);

		initialize();

		if (configURL != null)
			System.out.println("SSL Generic server started with config from " + configURL); //$NON-NLS-1$
		else
			System.out.println("SSL Generic server started with id=" + serverName); //$NON-NLS-1$

		waitForDone();

		return IApplication.EXIT_OK;
	}

	public void stop() {
		shutdown();
		synchronized (appLock) {
			done = true;
			appLock.notifyAll();
		}
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
