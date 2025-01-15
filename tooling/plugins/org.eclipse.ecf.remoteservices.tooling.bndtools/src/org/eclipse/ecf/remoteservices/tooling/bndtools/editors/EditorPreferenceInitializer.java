/*******************************************************************************
 * Copyright (c) 2020 Paul Verest, Benjamin Gurok, Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Apache Public License v2.0 which 
 * accompanies this distribution, and is available at 
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Contributors: Paul Verest, Benjamin Gurok, and Composent, Inc. - initial 
 * API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservices.tooling.bndtools.editors;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.ecf.remoteservices.tooling.bndtools.Activator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

/*
 * @author Benjamin gurok
 * @author Paul Verest
 */
public class EditorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        PreferenceConverter.setDefault(store, Constants.KEY_COLOR_COMMENT, Constants.DEFAULT_COLOR_COMMENT);
        PreferenceConverter.setDefault(store, Constants.KEY_COLOR_DOC, Constants.DEFAULT_COLOR_DOC);
        PreferenceConverter.setDefault(store, Constants.KEY_COLOR_KEYWORD, Constants.DEFAULT_COLOR_KEYWORD);
        PreferenceConverter.setDefault(store, Constants.KEY_COLOR_STRING, Constants.DEFAULT_COLOR_STRING);
        PreferenceConverter.setDefault(store, Constants.KEY_COLOR_NUMBER, Constants.DEFAULT_COLOR_NUMBER);
        PreferenceConverter.setDefault(store, Constants.KEY_COLOR_NORMAL, Constants.DEFAULT_COLOR_NORMAL);
        store.setDefault(Constants.KEY_BOLD_KEYWORD, true);	}

}

