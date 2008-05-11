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

package org.eclipse.ecf.internal.storage;

import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.storage.*;
import org.eclipse.equinox.security.storage.*;

/**
 *
 */
public class IDStore implements IIDStore {

	private static final String idStoreNameSegment = "/ECF/ID Store/"; //$NON-NLS-1$
	private static final String NSSEPARATOR = ":"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDStore#getNode(org.eclipse.ecf.core.identity.ID)
	 */
	public ISecurePreferences getNode(ID id) {
		if (id == null)
			return null;
		final ISecurePreferences root = SecurePreferencesFactory.getDefault();
		if (root == null)
			return null;
		final String idAsString = getIDAsString(id);
		if (idAsString == null)
			return null;
		final String path = idStoreNameSegment + EncodingUtils.encodeSlashes(idAsString);
		return root.node(path);
	}

	private String getIDAsString(ID id) {
		final Namespace ns = id.getNamespace();
		final INamespaceStoreAdapter nsadapter = (INamespaceStoreAdapter) ns.getAdapter(INamespaceStoreAdapter.class);
		final String nsName = (nsadapter != null) ? nsadapter.getNameForStorage() : ns.getName();
		if (nsName == null)
			return null;
		final IIDStoreAdapter idadapter = (IIDStoreAdapter) id.getAdapter(IIDStoreAdapter.class);
		final String idName = (idadapter != null) ? idadapter.getNameForStorage() : id.toExternalForm();
		if (idName == null || idName.equals("")) //$NON-NLS-1$
			return null;
		return nsName + NSSEPARATOR + idName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDStore#createID(org.eclipse.equinox.security.storage.ISecurePreferences)
	 */
	public ID createID(ISecurePreferences node) throws IDCreateException {
		if (node == null)
			throw new IDCreateException("Node cannot be null"); //$NON-NLS-1$
		final String nodeName = node.name();
		if (nodeName == null)
			throw new IDCreateException("Node name cannot be null"); //$NON-NLS-1$
		final int index = nodeName.indexOf(NSSEPARATOR);
		if (index == -1)
			throw new IDCreateException("Namespace name not found"); //$NON-NLS-1$
		final String nsName = nodeName.substring(0, index);
		final Namespace ns = IDFactory.getDefault().getNamespaceByName(nsName);
		if (ns == null)
			throw new IDCreateException("Namespace cannot be found"); //$NON-NLS-1$
		final String idName = nodeName.substring(index + NSSEPARATOR.length());
		return IDFactory.getDefault().createID(ns, idName);
	}
}
