/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.storage;

import org.eclipse.ecf.core.identity.IDCreateException;

/**
 * Adapter for retrieving ID name for storage.
 */
public interface IIDStoreAdapter {

	/**
	 * Get the ID name for storage.
	 * @return String name for storage.  Must not return <code>null</code>.
	 */
	public String getNameForStorage();

	/**
	 * Set the name of the ID instace from storage.
	 * @param valueFromStorage the value from {@link #getNameForStorage()}.
	 * @throws IDCreateException throws in valueFromStorage cannot be used to initialize
	 */
	public void initializeFromStorage(String valueFromStorage) throws IDCreateException;

}
