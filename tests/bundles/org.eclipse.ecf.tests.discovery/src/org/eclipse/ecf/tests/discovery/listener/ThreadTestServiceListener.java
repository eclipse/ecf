/****************************************************************************
 * Copyright (c) 2014 Markus Alexander Kuppe and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.discovery.listener;

import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceEvent;

public class ThreadTestServiceListener extends TestServiceListener {

	private volatile Thread currentThread;

	public Thread getCallingThread() {
		return currentThread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.tests.discovery.listener.TestServiceListener#triggerDiscovery()
	 */
	public boolean triggerDiscovery() {
		return true;
	}

	public ThreadTestServiceListener(int eventsToExpect,
			IDiscoveryLocator aLocator, String testName, String testId) {
		super(eventsToExpect, aLocator, testName, testId);
	}

	public void serviceDiscovered(IServiceEvent anEvent) {
		if (matchesExpected(anEvent)) {
			currentThread = Thread.currentThread();
			super.serviceDiscovered(anEvent);
		}
	}
}
