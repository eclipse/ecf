/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.discovery.listener;

import junit.framework.TestCase;

import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceTypeEvent;
import org.eclipse.ecf.discovery.IServiceTypeListener;

public class TestListener implements IServiceListener, IServiceTypeListener {

	private IContainerEvent event;
	
	/**
	 * @return the event that has been received by this TestListener
	 */
	public IContainerEvent getEvent() {
		return event;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceListener#serviceDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
	 */
	public void serviceDiscovered(IServiceEvent anEvent) {
		TestCase.assertNull("Received several IServiceEvent", event);
		event = anEvent;
		synchronized (this) {
			notifyAll();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceListener#serviceUndiscovered(org.eclipse.ecf.discovery.IServiceEvent)
	 */
	public void serviceUndiscovered(IServiceEvent anEvent) {
		throw new java.lang.UnsupportedOperationException("TestServiceListener#serviceUndiscovered not yet implemented");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceTypeListener#serviceTypeDiscovered(org.eclipse.ecf.discovery.IServiceTypeEvent)
	 */
	public void serviceTypeDiscovered(IServiceTypeEvent event) {
		TestCase.assertNull("Received several IServiceEvent", event);
		this.event = event;
		synchronized (this) {
			notifyAll();
		}
	}
}
