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

package org.eclipse.ecf.example.collab.ui;

import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ClientPreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

public ClientPreferencePage() {
    super(GRID);
    setPreferenceStore(ClientPlugin.getDefault().getPreferenceStore());
}

public void createFieldEditors() {
    addField(
        new BooleanFieldEditor(
            ClientPlugin.USE_CHAT_WINDOW,
            "Display ECF Collaboration in Separate Window",
            getFieldEditorParent()));
}

public void init(IWorkbench workbench) {
}
}