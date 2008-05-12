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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.storage.*;
import org.eclipse.equinox.security.storage.*;
import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class IDStore implements IIDStore {

	private static final String idStoreNameSegment = "/ECF/Namespace"; //$NON-NLS-1$
	private static final ISecurePreferences[] EMPTY_SECUREPREFERENCES = {};

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDStore#getNode(org.eclipse.ecf.core.identity.ID)
	 */
	public ISecurePreferences getNode(ID id) {
		ISecurePreferences namespaceRoot = getNamespaceRoot();
		if (namespaceRoot == null)
			return null;
		ISecurePreferences namespaceNode = getNamespaceNode(id.getNamespace());
		final String idAsString = getIDAsString(id);
		if (idAsString == null)
			return null;
		return namespaceNode.node(idAsString);
	}

	private String getIDAsString(ID id) {
		final IIDStoreAdapter idadapter = (IIDStoreAdapter) id.getAdapter(IIDStoreAdapter.class);
		final String idName = (idadapter != null) ? idadapter.getNameForStorage() : id.toExternalForm();
		if (idName == null || idName.equals("")) //$NON-NLS-1$
			return null;
		return EncodingUtils.encodeSlashes(idName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDStore#createID(org.eclipse.equinox.security.storage.ISecurePreferences)
	 */
	public ID createID(ISecurePreferences node) throws IDCreateException {
		if (node == null)
			throw new IDCreateException("Node cannot be null"); //$NON-NLS-1$
		String nsName = node.parent().name();
		final Namespace ns = IDFactory.getDefault().getNamespaceByName(nsName);
		if (ns == null)
			throw new IDCreateException(NLS.bind("Namespace {0} cannot be found", nsName)); //$NON-NLS-1$
		return IDFactory.getDefault().createID(ns, node.name());
	}

	protected ISecurePreferences getRoot() {
		return SecurePreferencesFactory.getDefault();
	}

	protected ISecurePreferences getNamespaceRoot() {
		ISecurePreferences root = getRoot();
		if (root == null)
			return null;
		return root.node(idStoreNameSegment);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDStore#getNamespaceNode(org.eclipse.ecf.core.identity.Namespace)
	 */
	public ISecurePreferences getNamespaceNode(Namespace namespace) {
		if (namespace == null)
			return null;
		final INamespaceStoreAdapter nsadapter = (INamespaceStoreAdapter) namespace.getAdapter(INamespaceStoreAdapter.class);
		final String nsName = (nsadapter != null) ? nsadapter.getNameForStorage() : namespace.getName();
		if (nsName == null)
			return null;
		ISecurePreferences namespaceRoot = getNamespaceRoot();
		if (namespaceRoot == null)
			return null;
		return namespaceRoot.node(nsName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDStore#getNamespaceNodes()
	 */
	public ISecurePreferences[] getNamespaceNodes() {
		ISecurePreferences namespaceRoot = getNamespaceRoot();
		if (namespaceRoot == null)
			return EMPTY_SECUREPREFERENCES;
		List results = new ArrayList();
		String names[] = namespaceRoot.childrenNames();
		for (int i = 0; i < names.length; i++)
			results.add(namespaceRoot.node(names[i]));
		return (ISecurePreferences[]) results.toArray(new ISecurePreferences[] {});
	}
}
