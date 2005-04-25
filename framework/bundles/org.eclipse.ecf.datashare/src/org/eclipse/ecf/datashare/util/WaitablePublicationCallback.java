/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare.util;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IPublicationCallback;
import org.eclipse.ecf.datashare.ISharedData;

/**
 * Convenience callback implementation that can be used to block the calling
 * thread until the data graph is published.
 * 
 * @author pnehrer
 */
public class WaitablePublicationCallback implements IPublicationCallback {

	private boolean published;

	private Throwable cause;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.sdo.IPublicationCallback#dataGraphPublished(org.eclipse.ecf.sdo.ISharedDataGraph)
	 */
	public synchronized void dataPublished(ISharedData graph) {
		published = true;
		notifyAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IPublicationCallback#publicationFailed(org.eclipse.ecf.datashare.ISharedData,
	 *      java.lang.Throwable)
	 */
	public synchronized void publicationFailed(ISharedData graph,
			Throwable cause) {
		this.cause = cause;
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
			throws InterruptedException, ECFException {
		if (!published && cause == null)
			wait(timeout);

		if (cause != null)
			throw new ECFException(cause);

		return published;
	}
}
