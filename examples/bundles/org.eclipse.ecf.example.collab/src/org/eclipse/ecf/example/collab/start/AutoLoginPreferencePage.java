package org.eclipse.ecf.example.collab.start;

import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class AutoLoginPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	public AutoLoginPreferencePage() {
		super(GRID);
		setPreferenceStore(ClientPlugin.getDefault().getPreferenceStore());
	}
	protected void createFieldEditors() {
		// TODO Auto-generated method stub
	}
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}
}
