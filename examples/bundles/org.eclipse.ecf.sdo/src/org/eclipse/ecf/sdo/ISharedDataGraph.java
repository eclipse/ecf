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
import org.eclipse.ecf.core.util.ECFException;

import commonj.sdo.DataGraph;

/**
 * Represents a shared data graph. Provides access to local data graph instance
 * and allows clients to share (commit) local changes with the network.
 * 
 * @author pnehrer
 */
public interface ISharedDataGraph {

    /**
     * Returns the shared data graph identifier.
     * 
     * @return id of the shared data graph
     */
    ID getID();

    /**
     * Returns local instance of the data graph.
     * 
     * @return local instance of the data graph
     */
    DataGraph getDataGraph();

    /**
     * Commits any outstanding local changes to the network.
     * 
     * @throws ECFException
     */
    void commit() throws ECFException;

    /**
     * Disposes this shared data graph. This will make it impossible to commit
     * any further changes. Also, no more remote updates will be received.
     */
    void dispose();
}
