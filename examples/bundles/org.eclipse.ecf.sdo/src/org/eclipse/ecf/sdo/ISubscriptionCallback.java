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
 * Interface used by service implementations to notify subscribing applications
 * when the initial copy of the data graph has been obtained.
 * 
 * @author pnehrer
 */
public interface ISubscriptionCallback {

    /**
     * Notifies the implementor that the given graph has been successfully
     * subscribed to.
     * 
     * @param graph
     *            shared data graph that has been subscribed
     * @param containerID
     *            id of the container that originated the initial copy
     */
    void dataGraphSubscribed(ISharedDataGraph graph, ID containerID);
}
