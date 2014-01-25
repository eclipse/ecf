package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.*;
import org.eclipse.ecf.internal.core.identity.Activator;

/**
 * @since 3.3
 */
public abstract class OptionalCodeSafeRunnable implements ISafeRunnable {

	public void handleException(Throwable exception) {
		Activator a = Activator.getDefault();
		if (a != null)
			a.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
					IStatus.WARNING,
					"Warning: optional code cannot be run", exception)); //$NON-NLS-1$
	}

	public abstract void run() throws Exception;

}
