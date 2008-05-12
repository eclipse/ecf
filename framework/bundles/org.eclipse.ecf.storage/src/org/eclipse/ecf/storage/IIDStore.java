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

import org.eclipse.ecf.core.identity.*;
import org.eclipse.equinox.security.storage.ISecurePreferences;

/**
 * ID storage service.  This interface defines access to storing a given ID instance as a node in
 * secure preferences.  It also provides a way to create an ID from a secure preferences node.
 */
public interface IIDStore {

	/**
	 * Get the namespace nodes exposed by this ID store.
	 * 
	 * @return array of namespace nodes for this ID store.  Will return <code>null</code> if
	 * secure preferences store not available.  Will return empty array if preferences available
	 * but no Namespaces have been added.  Each ISecurePreferences node represents a given {@link Namespace}, and
	 * the {@link ISecurePreferences#name()} entry will correspond to the {@link Namespace#getName()}.
	 */
	public ISecurePreferences[] getNamespaceNodes();

	/**
	 * Get the given namespace node for this ID store.
	 * 
	 * @param namespace the {@link Namespace} to get the node for.  Must not be <code>null</code>.
	 * @return the node for the given {@link Namespace}.  Will not return <code>null</code>.  If node
	 * previously was not present, it will be created.
	 */
	public ISecurePreferences getNamespaceNode(Namespace namespace);

	/**
	 * Get {@link ISecurePreferences} node for a given ID.  Clients may use this to either create an {@link ISecurePreferences} 
	 * instance for a new {@link ID}, or get an existing one from storage.
	 * @param id the ID to get the storage node for.
	 * @return ISecurePreferences for the given ID.  Will not return <code>null</code>.  
	 * Will return an existing node if ID is already present, and a new node if not.
	 */
	public ISecurePreferences getNode(ID id);

	/**
	 * Create ID for an ISecurePreferences node.
	 * @param node the node to use to create the new ID instance.  This node must not be <code>null</code>, and should
	 * be an instance previously created via {@link #getNode(ID)}.  It also must have a Namespace node as parent.
	 * @return new ID created.  Will not be <code>null</code>.
	 * @throws IDCreateException thrown if ID cannot be created...e.g. the given node does not define
	 * a valid ID instance.
	 */
	public ID createID(ISecurePreferences node) throws IDCreateException;

}
