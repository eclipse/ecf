/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
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
package org.eclipse.ecf.examples.internal.loadbalancing.ds.consumer;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class DataProcessorClientApplication implements IApplication {

	boolean done = false;
	Object appLock = new Object();

	public Object start(IApplicationContext context) throws Exception {
		// We just wait...everything is done by DS and HelloComponent
		synchronized (appLock) {
			while (!done) {
				try {
					appLock.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
		return IApplication.EXIT_OK;
	}

	public void stop() {
		synchronized (appLock) {
			done = true;
			appLock.notifyAll();
		}
	}

}
