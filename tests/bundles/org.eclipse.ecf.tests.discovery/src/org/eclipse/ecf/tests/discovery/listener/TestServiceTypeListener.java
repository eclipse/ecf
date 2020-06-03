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

import org.eclipse.ecf.discovery.IServiceTypeEvent;
import org.eclipse.ecf.discovery.IServiceTypeListener;

public class TestServiceTypeListener extends TestListener implements IServiceTypeListener {

	public TestServiceTypeListener(int eventsToExpect) {
		super(eventsToExpect);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceTypeListener#serviceTypeDiscovered(org.eclipse.ecf.discovery.IServiceTypeEvent)
	 */
	public synchronized void serviceTypeDiscovered(IServiceTypeEvent anEvent) {
		events.add(anEvent);
		if(events.size() == amountOfEventsToExpect) {
			synchronized (this) {
				notifyAll();
			}
		}
	}

}
