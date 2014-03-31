/*******************************************************************************
 * Copyright (c) 2014 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.*;
import org.eclipse.ecf.internal.core.identity.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @since 3.4
 */
public class AdapterManagerTracker extends ServiceTracker {

	public AdapterManagerTracker(BundleContext context,
			ServiceTrackerCustomizer customizer) {
		super(context, IAdapterManager.class.getName(), customizer);
	}

	public AdapterManagerTracker(BundleContext context) {
		this(context, null);
	}

	public IAdapterManager getAdapterManager() {
		IAdapterManager adapterManager = (IAdapterManager) getService();
		// Then, if the service isn't there, try to get from Platform class via
		// PlatformHelper class
		if (adapterManager == null)
			adapterManager = PlatformHelper.getPlatformAdapterManager();
		if (adapterManager == null)
			Activator.getDefault().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							IStatus.ERROR, "Cannot get adapter manager", null)); //$NON-NLS-1$
		return adapterManager;
	}

}
