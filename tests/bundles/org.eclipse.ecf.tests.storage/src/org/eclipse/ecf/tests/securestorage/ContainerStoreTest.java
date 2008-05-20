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

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.internal.tests.securestorage.Activator;
import org.eclipse.ecf.storage.IContainerEntry;
import org.eclipse.ecf.storage.IContainerStore;
import org.eclipse.ecf.storage.IStorableContainerAdapter;
import org.eclipse.equinox.security.storage.ISecurePreferences;

/**
 *
 */
public class ContainerStoreTest extends TestCase {

	IContainerStore containerStore;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		containerStore = Activator.getDefault().getContainerStore();
	}

	protected void clearStore() {
		final IContainerEntry[] containerEntries = containerStore.getContainerEntries();
		for (int i = 0; i < containerEntries.length; i++) {
			containerEntries[i].delete();
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		clearStore();
		containerStore = null;
	}

	public void testContainerStore() {
		assertNotNull(containerStore);
	}

	IContainer createContainer() throws ContainerCreateException {
		return ContainerFactory.getDefault().createContainer("ecf.storage.basecontainer");
	}

	IStorableContainerAdapter getStorableContainerAdapter(IContainer container) {
		return (IStorableContainerAdapter) container.getAdapter(IStorableContainerAdapter.class);
	}

	IContainerEntry storeContainer(IStorableContainerAdapter containerAdapter) {
		return containerStore.store(containerAdapter);
	}

	public void testStoreContainer() throws Exception {
		final IContainer container = createContainer();
		final IContainerEntry containerEntry = storeContainer(getStorableContainerAdapter(container));
		final ISecurePreferences prefs = containerEntry.getPreferences();
		assertNotNull(prefs);
	}

}
