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

package org.eclipse.ecf.internal.storage;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ecf.storage.IIDEntry;
import org.eclipse.ecf.storage.INamespaceEntry;
import org.eclipse.equinox.security.storage.ISecurePreferences;

/**
 *
 */
public class NamespaceEntry implements INamespaceEntry {

	private final ISecurePreferences prefs;

	public NamespaceEntry(ISecurePreferences prefs) {
		this.prefs = prefs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.INamespaceEntry#getIDEntries()
	 */
	public IIDEntry[] getIDEntries() {
		String[] names = prefs.childrenNames();
		List results = new ArrayList();
		for (int i = 0; i < names.length; i++)
			results.add(new IDEntry(prefs.node(names[i])));
		return (IIDEntry[]) results.toArray(new IIDEntry[] {});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.storage.INamespaceEntry#getPreferences()
	 */
	public ISecurePreferences getPreferences() {
		return prefs;
	}

	public void delete() {
		prefs.removeNode();
	}
}
