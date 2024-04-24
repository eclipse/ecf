package org.eclipse.ecf.provider.filetransfer.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.FrameworkUtil;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class FileTransferPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public FileTransferPreferencePage() {
		super(GRID);
		ScopedPreferenceStore scopedPreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, String.valueOf(FrameworkUtil.getBundle(getClass()).getBundleId()));
		setPreferenceStore(scopedPreferenceStore);
		setDescription("This option allows you to configure a request header property/value pair.\n" //$NON-NLS-1$
				+ " It is used during a connection request.\n\n"); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {

		addField(new StringFieldEditor(PreferenceConstants.REQUEST_CONN_PROPERTY, "Request Connection:", getFieldEditorParent())); //$NON-NLS-1$
		addField(new StringFieldEditor(PreferenceConstants.REQUEST_VALUE_PROPERTY, "Connection Value:", getFieldEditorParent())); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {

	}

}