/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.sdo;

/**
 * Convenience callback implementation that can be used to block the calling
 * thread until the data graph is published.
 * 
 * @author pnehrer
 */
public class WaitablePublicationCallback implements IPublicationCallback {

	private boolean published;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.sdo.IPublicationCallback#dataGraphPublished(org.eclipse.ecf.sdo.ISharedDataGraph)
	 */
	public synchronized void dataGraphPublished(ISharedDataGraph graph) {
		published = true;
		notifyAll();
	}

	/**
	 * Blocks the calling thread until the data graph is published.
	 * 
	 * @param timeout
	 *            period, in milliseconds, to wait for publication
	 * @throws InterruptedException
	 *             if interrupted while waiting for notification
	 */
	public synchronized boolean waitForPublication(long timeout)
			throws InterruptedException {
		if (!published)
			wait(timeout);

		return published;
	}
}
