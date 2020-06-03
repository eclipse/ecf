/****************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.discovery.listener;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.core.events.IContainerEvent;

public class TestListener {

	protected List events;
	protected int amountOfEventsToExpect;
	
	public TestListener(int eventsToExpect) {
		amountOfEventsToExpect = eventsToExpect;
		events = new ArrayList(eventsToExpect);
	}

	/**
	 * @return the event that has been received by this TestListener
	 */
	public IContainerEvent[] getEvent() {
		return (IContainerEvent[]) events.toArray(new IContainerEvent[0]);
	}
}
