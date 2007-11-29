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

import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceListener;

public class TestServiceListener extends AbstractTestListener implements IServiceListener {

	private IServiceEvent event;

	public TestServiceListener() {
		super();
	}

	public boolean isDone() {
		return event != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceListener#serviceDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
	 */
	public void serviceDiscovered(IServiceEvent anEvent) {
		event = anEvent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceListener#serviceUndiscovered(org.eclipse.ecf.discovery.IServiceEvent)
	 */
	public void serviceUndiscovered(IServiceEvent anEvent) {
		//TODO-mkuppe implement TestServiceListener#serviceUndiscovered
		throw new java.lang.UnsupportedOperationException("TestServiceListener#serviceUndiscovered not yet implemented");
	}

	/**
	 * @return the event
	 */
	public IServiceEvent getEvent() {
		return event;
	}
}
