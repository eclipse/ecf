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
package org.eclipse.ecf.datashare;

import org.eclipse.ecf.core.util.ECFException;

/**
 * <p>
 * Interface used by service implementations to manage SDO
 * implementation-specific data graph updates. Upon commit, the service needs to
 * create an update (an arbitrary serializable object), which it then propagates
 * across the network. On the other end, the service needs to apply the received
 * update to the local data graph.
 * </p>
 * <p>
 * Until serialization-related issues within ECF are resolved, the service also
 * needs to delegate data graph (de)serialization during subscription.
 * </p>
 * 
 * @author pnehrer
 */
public interface IUpdateProvider {
	
	IUpdateProviderFactory getFactory();
	
	/**
	 * Creates an update from the given data graph, which will be forwarded to
	 * other group members. The implementor may use the graph's Change Summary
	 * to find out what changed.
	 * 
	 * @param graph
	 *            shared data graph from whose changes to create the update
	 * @return serialized update data
	 * @throws ECFException
	 *             when an update cannot be created
	 */
	Object createUpdate(ISharedData graph) throws ECFException;

	/**
	 * Applies the remote update to the given data graph. The implementor is
	 * expected to create a Change Summary that reflects the received changes.
	 * 
	 * @param graph
	 *            local data graph to which to apply the update
	 * @param data
	 *            update data received from a remote group member
	 * @throws ECFException
	 *             when this update cannot be applied
	 */
	void applyUpdate(ISharedData graph, Object data) throws ECFException;
}
