/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core;

import org.eclipse.ecf.core.identity.ID;

/**
 * Defines implementing classes as being identifiable with
 * an ECF identity.  
 *
 */
public interface IIdentifiable {

    /**
     * Return the ID for this 'identifiable' object. The returned ID should be
     * unique within its namespace.
     * @return the ID for this identifiable object
     */
    public ID getID();
}
