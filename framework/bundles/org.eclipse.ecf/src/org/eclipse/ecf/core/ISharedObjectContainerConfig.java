/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core;

import java.util.Map;

import org.eclipse.ecf.core.identity.ID;

public interface ISharedObjectContainerConfig {

    /**
     * The ID of the owner ISharedObjectContainer. Must be non-null, and the ID
     * must be unique within the namespace of the relevant set of
     * ISharedObjectContainer instances.
     * 
     * @return ID the non-null ID instance that uniquely identifies the
     *         ISharedObjectContainer instance that uses this config.
     */
    public ID getID();
    /**
     * The properties associated with the owner ISharedObjectContainer
     * 
     * @return Map the properties associated with owner
     *         ISharedObjectContainer
     */
    public Map getProperties();
    /**
     * Returns an object which is an instance of the given class associated with
     * this object.
     * 
     * @param adapter
     *            the adapter class to lookup
     * @return Object a object castable to the given class, or null if this
     *         object does not have an adapter for the given class
     */
    public Object getAdapter(Class clazz);
}