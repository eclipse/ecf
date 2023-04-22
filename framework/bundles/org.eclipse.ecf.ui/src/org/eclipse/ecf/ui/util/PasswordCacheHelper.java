/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.ui.util;

import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.equinox.security.storage.*;

/**
 * Helper for caching password via
 * Platform#addAuthorizationInfo(URL, String, String, Map)
 */
public class PasswordCacheHelper {

	private static final String TOP_NODE = "org.eclipse.ecf.ui"; //$NON-NLS-1$
	public static final String INFO_PASSWORD = "org.eclipse.ecf.ui.password"; //$NON-NLS-1$
	private ISecurePreferences securePrefs;

	public PasswordCacheHelper(String targetID) {
		this.securePrefs = SecurePreferencesFactory.getDefault().node(TOP_NODE).node(targetID);
	}

	public boolean savePassword(String password) {
		try {
			this.securePrefs.put(INFO_PASSWORD, password, true);
		} catch (StorageException e) {
			Activator.log("savePassword", e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	public String retrievePassword() {
		try {
			return this.securePrefs.get(INFO_PASSWORD, null);
		} catch (StorageException e) {
			Activator.log("savePassword", e); //$NON-NLS-1$
			return null;
		}
	}
}
