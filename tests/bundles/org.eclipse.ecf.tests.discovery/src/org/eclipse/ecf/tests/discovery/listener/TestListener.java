/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.tests.discovery.listener;

import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceTypeEvent;
import org.eclipse.ecf.discovery.IServiceTypeListener;

public class TestListener implements IServiceListener, IServiceTypeListener {

	private IContainerEvent event;
	private int eventCount;
	private int amountOfEventsToExpect;
	
	public TestListener(int eventsToExpect) {
		amountOfEventsToExpect = eventsToExpect;
	}

	/**
	 * @return the event that has been received by this TestListener
	 */
	public IContainerEvent getEvent() {
		return event;
	}

	/**
	 * @return The amount of events sent to this TestListener
	 */
	public int getEventCount() {
		return eventCount;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceListener#serviceDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
	 */
	public void serviceDiscovered(IServiceEvent anEvent) {
		eventCount++;
		event = anEvent;
		if(eventCount == amountOfEventsToExpect) {
			synchronized (this) {
				notifyAll();
			}
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
	public void serviceTypeDiscovered(IServiceTypeEvent anEvent) {
		eventCount++;
		event = anEvent;
		if(eventCount == amountOfEventsToExpect) {
			synchronized (this) {
				notifyAll();
			}
		}
	}
}
