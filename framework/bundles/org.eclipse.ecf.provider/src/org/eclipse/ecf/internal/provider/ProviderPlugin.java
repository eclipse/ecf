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

package org.eclipse.ecf.internal.provider;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.core.util.LogHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The main plugin class to be used in the desktop.
 */
public class ProviderPlugin implements BundleActivator {
	
	public static final String PLUGIN_ID = "org.eclipse.ecf.provider"; //$NON-NLS-1$
	
    //The shared instance.
    private static ProviderPlugin plugin;

    public static final String NAMESPACE_IDENTIFIER = org.eclipse.ecf.core.identity.StringID.class.getName();
    
	private BundleContext context = null;

	private ServiceTracker logServiceTracker = null;

    /**
     * The constructor.
     */
    public ProviderPlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
		}
		this.context = null;
    }

	protected LogService getLogService() {
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context,
					LogService.class.getName(), null);
			logServiceTracker.open();
		}
		return (LogService) logServiceTracker.getService();
	}

	public void log(IStatus status) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(LogHelper.getLogCode(status), LogHelper
					.getLogMessage(status), status.getException());
		}
	}

	public IAdapterManager getAdapterManager() {
		// XXX todo...replace with new adaptermanager service
		return Platform.getAdapterManager();
		//return null;
	}

    /**
     * Returns the shared instance.
     */
    public static ProviderPlugin getDefault() {
        return plugin;
    }

    public String getNamespaceIdentifier() {
    	return NAMESPACE_IDENTIFIER;
    }
}