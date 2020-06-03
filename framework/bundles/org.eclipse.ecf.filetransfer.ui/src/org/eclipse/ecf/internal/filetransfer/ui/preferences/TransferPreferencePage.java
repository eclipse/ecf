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

package org.eclipse.ecf.internal.filetransfer.ui.preferences;

import org.eclipse.ecf.internal.filetransfer.ui.Activator;
import org.eclipse.ecf.internal.filetransfer.ui.Messages;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 *
 */
public class TransferPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public TransferPreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		addField(new DirectoryFieldEditor(Activator.DOWNLOAD_PATH_PREFERENCE, Messages.getString("DownloadPreferencePage_SAVE_FILES_FIELD_EDITOR_TEXT"), getFieldEditorParent())); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		getPreferenceStore().setDefault(Activator.DOWNLOAD_PATH_PREFERENCE, Activator.getDefault().getDefaultDownloadPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// nothing to do
	}
}
