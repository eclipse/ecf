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

import org.eclipse.ecf.core.identity.ID;

/**
 * Convenience callback implementation that can be used to block the calling
 * thread until the data graph is obtained.
 * 
 * @author pnehrer
 */
public class SubscriptionBlocker implements ISubscriptionCallback {

    private boolean subscribed;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ecf.sdo.ISubscriptionCallback#dataGraphObtained(org.eclipse.ecf.sdo.ISharedDataGraph,
     *      org.eclipse.ecf.core.identity.ID)
     */
    public synchronized void dataGraphSubscribed(ISharedDataGraph graph,
            ID containerID) {
        subscribed = true;
        notifyAll();
    }

    /**
     * Blocks the calling thread until the data graph is obtained.
     * 
     * @param timeout
     *            period, in milliseconds, to wait for subscription
     * @return <code>true</code> if the data graph has been obtained
     * @throws InterruptedException
     */
    public synchronized boolean waitForSubscription(long timeout)
            throws InterruptedException {
        if (!subscribed)
            wait(timeout);

        return subscribed;
    }
}
