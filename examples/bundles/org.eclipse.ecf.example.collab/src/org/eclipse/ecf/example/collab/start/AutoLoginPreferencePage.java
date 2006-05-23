package org.eclipse.ecf.example.collab.start;

import java.util.Collection;
import org.eclipse.ecf.example.collab.ClientPlugin;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class AutoLoginPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	protected Collection contents = null;
	protected ListViewer list = null;
	protected Button delete = null;
	
	public AutoLoginPreferencePage() {
		super(GRID);
		setPreferenceStore(ClientPlugin.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
	}
	protected void createFieldEditors() {
		addField(new URLListFieldEditor("urilisteditor","The following will be connected upon ECF start",getFieldEditorParent()));
	}
}
