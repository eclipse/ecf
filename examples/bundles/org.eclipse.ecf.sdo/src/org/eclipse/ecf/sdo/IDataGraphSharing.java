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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ecf.core.identity.ID;

import commonj.sdo.DataGraph;

/**
 * Allows clients to participate in data graph sharing. A container-specific
 * instance may be obtained by calling
 * {@link org.eclipse.ecf.sdo.SDOPlugin#getDataGraphSharing(org.eclipse.ecf.ISharedObjectContainer) SDOPlugin.getDataGraphSharing(&lt;container&gt;)}.
 * 
 * @author pnehrer
 */
public interface IDataGraphSharing {

    /**
     * Publishes the given data graph under the given id.
     * 
     * @param dataGraph
     *            local data graph instance to share
     * @param id
     *            identifier under which to share this data graph
     * @param provider
     *            update provider compatible with the given data graph's
     *            implementation
     * @param consumer
     *            application-specific update consumer
     * @return shared data graph
     * @throws CoreException
     */
    ISharedDataGraph publish(DataGraph dataGraph, ID id,
            IUpdateProvider provider, IUpdateConsumer consumer)
            throws CoreException;

    /**
     * Subscribes to a data graph with the given id.
     * 
     * @param id
     *            identifier of a previously-published data graph
     * @param callback
     *            callback used to notify the caller when the subscription
     *            completes
     * @param provider
     *            update provider compatible with the given data graph's
     *            implementation
     * @param consumer
     *            application-specific update consumer
     * @return shared data graph
     * @throws CoreException
     */
    ISharedDataGraph subscribe(ID id, ISubscriptionCallback callback,
            IUpdateProvider provider, IUpdateConsumer consumer)
            throws CoreException;

    /**
     * Disposes this instance, after which it will be no longer possible to
     * publish or subscribe.
     */
    void dispose();
}
