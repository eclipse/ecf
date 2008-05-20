/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.storage;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Storage interface for IContainer instances.
 */
public interface IContainerStore extends IAdaptable {

	/**
	 * Get all {@link IContainerEntry}s.  Will return from secure storage all container entries previously added
	 * via {@link #store(IStorableContainerAdapter)}.
	 * 
	 * @return IContainerEntry[] of all container entries.  Will not return <code>null</code>.  If no containers previously
	 * stored, will return empty array.
	 */
	public IContainerEntry[] getContainerEntries();

	/**
	 * Store a {@link IStorableContainerAdapter} in this container store.
	 * 
	 * @param containerAdapter the {@link IStorableContainerAdapter} to store.  Must not be <code>null</code>.
	 * @return {@link IContainerEntry} result of storage.  Will not return <code>null</code>.
	 */
	public IContainerEntry store(IStorableContainerAdapter containerAdapter);

}
