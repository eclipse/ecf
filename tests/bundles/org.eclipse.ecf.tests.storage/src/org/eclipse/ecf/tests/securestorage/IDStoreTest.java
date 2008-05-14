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

package org.eclipse.ecf.tests.securestorage;

import junit.framework.TestCase;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.internal.tests.securestorage.Activator;
import org.eclipse.ecf.storage.IIDEntry;
import org.eclipse.ecf.storage.IIDStore;
import org.eclipse.ecf.storage.INamespaceEntry;
import org.eclipse.equinox.security.storage.ISecurePreferences;

/**
 *
 */
public class IDStoreTest extends TestCase {

	IIDStore idStore;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		idStore = Activator.getDefault().getIDStore();
	}

	protected void clearStore() {
		final INamespaceEntry[] namespaces = idStore.getNamespaceEntries();
		for (int i = 0; i < namespaces.length; i++) {
			namespaces[i].delete();
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		clearStore();
		idStore = null;
	}

	public void testIDStore() {
		assertNotNull(idStore);
	}

	protected IIDEntry addGUID() throws IDCreateException {
		final ID newGUID = IDFactory.getDefault().createGUID();
		return idStore.getEntry(newGUID);
	}

	protected IIDEntry addStringID(String value) throws IDCreateException {
		final ID newID = IDFactory.getDefault().createStringID(value);
		return idStore.getEntry(newID);
	}

	public void testStoreGUID() throws Exception {
		final ISecurePreferences prefs = addGUID().getPreferences();
		assertNotNull(prefs);
	}

	public void testStoreGUIDs() throws Exception {
		testStoreGUID();
		testStoreGUID();
		testStoreGUID();
	}

	public void testListEmptyNamespaces() throws Exception {
		final INamespaceEntry[] namespaces = idStore.getNamespaceEntries();
		assertNotNull(namespaces);
	}

	public void testOneNamespace() throws Exception {
		testStoreGUID();
		testStoreGUID();
		final INamespaceEntry[] namespaces = idStore.getNamespaceEntries();
		assertTrue(namespaces.length == 1);
	}

	public void testTwoNamespace() throws Exception {
		testStoreGUID();
		addStringID("1");
		final INamespaceEntry[] namespaces = idStore.getNamespaceEntries();
		assertTrue(namespaces.length == 2);
	}

	public void testGetNamespaceNode() throws Exception {
		final ID newGUID = IDFactory.getDefault().createGUID();
		idStore.getEntry(newGUID);
		final ISecurePreferences namespacePrefs = idStore.getNamespaceEntry(newGUID.getNamespace()).getPreferences();
		assertNotNull(namespacePrefs);
		assertTrue(namespacePrefs.name().equals(newGUID.getNamespace().getName()));
	}

	public void testGetIDEntries() throws Exception {
		final ID newGUID = IDFactory.getDefault().createGUID();
		idStore.getEntry(newGUID);
		// Get namespace entry
		final INamespaceEntry namespaceEntry = idStore.getNamespaceEntry(newGUID.getNamespace());
		assertNotNull(namespaceEntry);

		final IIDEntry[] idEntries = namespaceEntry.getIDEntries();
		assertTrue(idEntries.length == 1);
		// Create GUID from idEntry
		final ID persistedGUID = idEntries[0].createID();
		assertNotNull(persistedGUID);
		assertTrue(persistedGUID.equals(newGUID));
	}

	public void testCreateAssociation() throws Exception {
		// Create two GUIDs and store them in idStore
		final ID guid1 = IDFactory.getDefault().createGUID();
		final IIDEntry entry1 = idStore.getEntry(guid1);
		final ID guid2 = IDFactory.getDefault().createGUID();
		final IIDEntry entry2 = idStore.getEntry(guid2);

		// Create association
		entry1.addAssociateIDEntry(entry2, true);

		// Get entry1a
		final IIDEntry entry1a = idStore.getEntry(guid1);
		assertNotNull(entry1a);
		// Get associates (should include entry2)
		final IIDEntry[] entries = entry1a.getAssociateIDEntries();
		assertNotNull(entries);
		assertTrue(entries.length == 1);
		// entry2a should be same as entry2
		final IIDEntry entry2a = entries[0];
		assertNotNull(entry2a);
		final ID guid2a = entry2a.createID();
		// and guid2a should equal guid2
		assertTrue(guid2.equals(guid2a));

	}
}
