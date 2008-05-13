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
}
