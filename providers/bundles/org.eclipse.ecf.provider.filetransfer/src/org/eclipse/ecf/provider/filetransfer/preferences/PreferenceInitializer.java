package org.eclipse.ecf.provider.filetransfer.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.FrameworkUtil;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, String.valueOf(FrameworkUtil.getBundle(getClass()).getBundleId()));

		store.setDefault(PreferenceConstants.REQUEST_CONN_PROPERTY, "Connection"); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.REQUEST_VALUE_PROPERTY, "close"); //$NON-NLS-1$
	}

}
