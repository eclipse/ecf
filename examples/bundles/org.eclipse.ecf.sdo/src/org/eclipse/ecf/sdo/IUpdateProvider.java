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

import java.io.IOException;

import org.eclipse.ecf.core.util.ECFException;

import commonj.sdo.DataGraph;

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

	/**
	 * Creates an update from the given data graph, which will be forwarded to
	 * other group members. The implementor may use the graph's Change Summary
	 * to find out what changed.
	 * 
	 * @param graph
	 *            shared data graph from whose changes to create the update
	 * @return serialized update data
	 * @throws ECFException when an update cannot be created
	 */
	byte[] createUpdate(ISharedDataGraph graph) throws ECFException;

	/**
	 * Applies the remote update to the given data graph. The implementor is
	 * expected to create a Change Summary that reflects the received changes.
	 * 
	 * @param graph
	 *            local data graph to which to apply the update
	 * @param data
	 *            update data received from a remote group member
	 * @throws ECFException when this update cannot be applied
	 */
	void applyUpdate(ISharedDataGraph graph, Object data) throws ECFException;

	/**
	 * Serializes the given data graph.
	 * 
	 * @param graph
	 *            data graph instance to serialize
	 * @return serialized data graph
	 * @throws IOException
	 */
	Object serializeDataGraph(DataGraph graph) throws IOException;

	/**
	 * Deserializes the given data graph.
	 * 
	 * @param data
	 *            serialized data graph
	 * @return deserialized instance of data graph
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	DataGraph deserializeDataGraph(Object data) throws IOException,
			ClassNotFoundException;
}
