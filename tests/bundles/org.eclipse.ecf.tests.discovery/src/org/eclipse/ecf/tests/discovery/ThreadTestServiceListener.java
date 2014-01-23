/*******************************************************************************
 * Copyright (c) 2014 Markus Alexander Kuppe and others. All rights reserved. 
 * This program and the accompanying materials are made available under the terms 
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.discovery;

import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.tests.discovery.listener.TestServiceListener;

public class ThreadTestServiceListener extends TestServiceListener {

	private Thread currentThread;

	public Thread getCallingThread() {
		return currentThread;
	}

	public ThreadTestServiceListener(int eventsToExpect,
			IDiscoveryLocator aLocator) {
		super(eventsToExpect, aLocator);
	}

	public void serviceDiscovered(IServiceEvent anEvent) {
		super.serviceDiscovered(anEvent);
		currentThread = Thread.currentThread();
	}
}
