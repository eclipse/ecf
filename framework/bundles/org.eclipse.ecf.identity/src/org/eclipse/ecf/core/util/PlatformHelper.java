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

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.internal.core.identity.Activator;

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
			platformClass = Class.forName(
					"org.eclipse.core.runtime.Platform"); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			// Platform not available...just leave platformClass == null
		}
	}

	public synchronized static boolean isPlatformAvailable() {
		return platformClass != null;
	}

	public synchronized static IAdapterManager getPlatformAdapterManager() {
		if (adapterManagerCache != null)
			return adapterManagerCache;
		if (!isPlatformAvailable()) {
			Activator
					.getDefault()
					.log(
							new Status(
									IStatus.ERROR,
									Activator.PLUGIN_ID,
									IStatus.ERROR,
									"org.eclipse.core.runtime.Platform class not available", //$NON-NLS-1$
									null));
			return null;
		} else {
			try {
				Method m = platformClass.getMethod("getAdapterManager", null); //$NON-NLS-1$
				adapterManagerCache = (IAdapterManager) m.invoke(null, null);
				return adapterManagerCache;
			} catch (Exception e) {
				Activator.getDefault().log(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								IStatus.ERROR,
								"exception in getPlatformAdapterManager", e)); //$NON-NLS-1$
				return null;
			}
		}
	}

	public synchronized static IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistryCache != null)
			return extensionRegistryCache;
		if (!isPlatformAvailable()) {
			Activator
					.getDefault()
					.log(
							new Status(
									IStatus.ERROR,
									Activator.PLUGIN_ID,
									IStatus.ERROR,
									"org.eclipse.core.runtime.Platform class not available", //$NON-NLS-1$
									null));
			return null;
		} else {
			try {
				Method m = platformClass
						.getMethod("getExtensionRegistry", null); //$NON-NLS-1$
				extensionRegistryCache = (IExtensionRegistry) m.invoke(null,
						null);
				return extensionRegistryCache;
			} catch (Exception e) {
				Activator.getDefault().log(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								IStatus.ERROR,
								"exception in getExtensionRegistry", e)); //$NON-NLS-1$
				return null;
			}
		}
	}

}
