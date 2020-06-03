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

/**
 * Adapter for retrieving Namespace name for storage.
 */
public interface INamespaceStoreAdapter {

	/**
	 * Get the Namespace name for storage.
	 * @return String name for storage.  Must not return <code>null</code>.
	 */
	public String getNameForStorage();

}
