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

import java.util.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.internal.storage.Activator;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class IDEntry implements IIDEntry {

	/**
	 * 
	 */
	private static final String DELIMITER = ":"; //$NON-NLS-1$

	private final ISecurePreferences prefs;

	private final String ASSOCIATE_IDENTRIES_NODE = "associates"; //$NON-NLS-1$

	public IDEntry(ISecurePreferences prefs) {
		this.prefs = prefs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDEntry#getPreferences()
	 */
	public ISecurePreferences getPreferences() {
		return prefs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDEntry#addAssociateIDEntry(org.eclipse.ecf.storage.IIDEntry,boolean encrypt)
	 */
	public void addAssociateIDEntry(IIDEntry entry, boolean encrypt) throws IDStoreException {
		ISecurePreferences associateNode = prefs.node(ASSOCIATE_IDENTRIES_NODE);
		String entryAssociate = createAssociateName(entry);
		associateNode.node(entryAssociate);
	}

	private String createAssociateName(IIDEntry entry) throws IDStoreException {
		ISecurePreferences prefs = entry.getPreferences();
		return prefs.parent().name() + DELIMITER + prefs.name();
	}

	private ISecurePreferences getNamespaceRoot() {
		// The namespace root is the parent of our parent (which is the namespace)
		return prefs.parent().parent();
	}

	private ISecurePreferences getPreferences(ISecurePreferences parent, String name) {
		List names = Arrays.asList(parent.childrenNames());
		if (names.contains(name))
			return parent.node(name);
		return null;
	}

	private IIDEntry createAssociateFromName(String name) throws IDStoreException {
		int index = name.indexOf(DELIMITER);
		if (index == -1)
			throw new IDStoreException("Associate ID not well-formed"); //$NON-NLS-1$
		try {
			String namespaceName = name.substring(0, index);
			ISecurePreferences namespacePrefs = getPreferences(getNamespaceRoot(), namespaceName);
			if (namespacePrefs == null)
				throw new IDStoreException(NLS.bind("Cannot find Namespace {0}", namespaceName)); //$NON-NLS-1$
			String idName = name.substring(index + 1);
			ISecurePreferences idPrefs = getPreferences(namespacePrefs, idName);
			if (idPrefs == null)
				throw new IDStoreException(NLS.bind("ID {0} not found in Namespace {1}", idName, namespaceName)); //$NON-NLS-1$
			return new IDEntry(idPrefs);
		} catch (IndexOutOfBoundsException e) {
			throw new IDStoreException("Associate ID not well-formed"); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDEntry#createID()
	 */
	public ID createID() throws IDCreateException {
		return IDFactory.getDefault().createID(prefs.parent().name(), prefs.name());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDEntry#delete()
	 */
	public void delete() {
		prefs.removeNode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.IIDEntry#getAssociateIDEntries()
	 */
	public IIDEntry[] getAssociateIDEntries() {
		ISecurePreferences associateNode = prefs.node(ASSOCIATE_IDENTRIES_NODE);
		String[] childrenNames = associateNode.childrenNames();
		List results = new ArrayList();
		for (int i = 0; i < childrenNames.length; i++) {
			try {
				IIDEntry associateEntry = createAssociateFromName(childrenNames[i]);
				results.add(associateEntry);
			} catch (IDStoreException e) {
				Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Unable to create associate ID", e)); //$NON-NLS-1$
			}
		}
		return (IIDEntry[]) results.toArray(new IIDEntry[] {});
	}
}
