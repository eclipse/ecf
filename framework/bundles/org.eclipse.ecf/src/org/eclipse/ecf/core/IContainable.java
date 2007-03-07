/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.core;

/**
 * Objects that can have 0 or more {@link IContainer} parents.
 */
public interface IContainable {

	/**
	 * Get the containers for the implementing containable object.
	 * 
	 * @return IContainer[] the container array for this containable.  If this
	 * instance has no containers associated with it, then an array of length 0
	 * will be returned.  Will not return <code>null</code>.
	 */
	public IContainer[] getContainers();

}
