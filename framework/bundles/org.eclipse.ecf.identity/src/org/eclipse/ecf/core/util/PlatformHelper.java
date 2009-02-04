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

package org.eclipse.ecf.core.util;

import java.lang.reflect.Method;
import org.eclipse.core.runtime.*;
import org.eclipse.ecf.internal.core.identity.Activator;
import org.osgi.framework.Bundle;

/**
 * Helper class for eliminating direct references to Platform static methods
 * getAdapterManager and getExtensionRegistry. Note that instead of
 * Platform.getAdapterManager(), clients can call
 * PlatformHelper.getAdapterManager(). If this returns null, the Platform class
 * is not available.
 */
public class PlatformHelper {

	private static Class platformClass = null;

	private static IAdapterManager adapterManagerCache = null;

	private static IExtensionRegistry extensionRegistryCache = null;

	static {
		try {
			Bundle[] bundles = Activator.getDefault().getBundleContext().getBundles();
			Bundle coreRuntime = null;
			for (int i = 0; i < bundles.length; i++)
				if (bundles[i].getSymbolicName().equals("org.eclipse.core.runtime")) { //$NON-NLS-1$
					coreRuntime = bundles[i];
					platformClass = coreRuntime.loadClass("org.eclipse.core.runtime.Platform"); //$NON-NLS-1$
					break;
				}
		} catch (Exception e) {
			// Platform not available...just leave platformClass == null and log
			// as error
			try {
				Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Cannot load Platform class.  No adapter manager available", //$NON-NLS-1$
						e));
			} catch (Throwable t) {
				// Should never happen, if does irrecoverable
			}
		}
	}

	public synchronized static boolean isPlatformAvailable() {
		return platformClass != null;
	}

	public synchronized static IAdapterManager getPlatformAdapterManager() {
		if (adapterManagerCache != null)
			return adapterManagerCache;
		if (isPlatformAvailable()) {
			try {
				Method m = platformClass.getMethod("getAdapterManager", (Class []) null); //$NON-NLS-1$
				adapterManagerCache = (IAdapterManager) m.invoke(null, (Object []) null);
				return adapterManagerCache;
			} catch (Exception e) {
				Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Exception in PlatformHelper.getPlatformAdapterManager()", e)); //$NON-NLS-1$
				return null;
			}
		}
		return null;
	}

	public synchronized static IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistryCache != null)
			return extensionRegistryCache;
		if (isPlatformAvailable()) {
			try {
				Method m = platformClass.getMethod("getExtensionRegistry", (Class []) null); //$NON-NLS-1$
				extensionRegistryCache = (IExtensionRegistry) m.invoke(null, (Object []) null);
				return extensionRegistryCache;
			} catch (Exception e) {
				Activator.getDefault().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, "Exception in PlatformHelper.getExtensionRegistry()", e)); //$NON-NLS-1$
				return null;
			}

		}
		return null;
	}
}
