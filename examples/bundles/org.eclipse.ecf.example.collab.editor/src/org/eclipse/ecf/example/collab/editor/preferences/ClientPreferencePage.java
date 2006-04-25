/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.example.collab.editor.preferences;

import org.eclipse.ecf.example.collab.editor.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ClientPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {


	public static final String CONTAINER_TYPE = "CONTAINER_TYPE";
	public static final String TARGET_SERVER = "TARGET_SERVER";
	public static final String CHANNEL_ID = "CHANNEL_ID";
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		
		getPreferenceStore().setDefault(CONTAINER_TYPE, "ecf.generic.channel");
		getPreferenceStore().setDefault(TARGET_SERVER, "ecftcp://localhost:3282/server");
		getPreferenceStore().setDefault(CHANNEL_ID, "collab.editor");
	}
	
	public ClientPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	public void createFieldEditors() {
		addField(new StringFieldEditor(CONTAINER_TYPE, "Container Type:", this.getFieldEditorParent()));
		addField(new StringFieldEditor(TARGET_SERVER, "ECF Server URL:", this.getFieldEditorParent()));
		addField(new StringFieldEditor(CHANNEL_ID, "Channel (Group) Name:", this.getFieldEditorParent()));		
	}
	
	public void init(IWorkbench workbench) {
		
	}
	
	public void initializeDefaults() {
		performDefaults();
	}
}