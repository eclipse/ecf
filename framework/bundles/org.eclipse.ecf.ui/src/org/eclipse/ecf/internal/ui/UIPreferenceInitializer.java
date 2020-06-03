/****************************************************************************
 * Copyright (c) 2004, 2007 Remy Suen, Composent, Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class UIPreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		//Activator.getDefault().getPreferenceStore().setDefault(ChatPreferencePage.PREF_BROWSER_FOR_CHAT, ChatPreferencePage.VIEW);
	}

}
