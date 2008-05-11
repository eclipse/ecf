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

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.equinox.security.storage.ISecurePreferences;

/**
 * ID storage service.  This interface defines access to storing a given ID instance as a node in
 * secure preferences.  It also provides a way to create an ID from a secure preferences node.
 */
public interface IIDStore {

	/**
	 * Get {@link ISecurePreferences} for all IDs in ID store.
	 * 
	 * @return array of ISecurePreferences instances.  If number of instances current stored is 0, returns
	 * empty array.  Will not return <code>null</code>.
	 */
	public ISecurePreferences[] getNodes();

	/**
	 * Get {@link ISecurePreferences} node for a given ID.  Clients may use this to either create an {@link ISecurePreferences} 
	 * instance for a new {@link ID}, or get an existing one from storage.
	 * @param id the ID to get the storage node for.
	 * @return ISecurePreferences for the given ID.  Will return an existing node if ID is already present, and a new
	 * node if not.
	 */
	public ISecurePreferences getNode(ID id);

	/**
	 * Create ID for an ISecurePreferences node.
	 * @param node the node to use to create the new ID instance.
	 * @return new ID created.  Will not be <code>null</code>.
	 * @throws IDCreateException thrown if ID cannot be created...e.g. the given node does not define
	 * a valid ID instance.
	 */
	public ID createID(ISecurePreferences node) throws IDCreateException;

}
