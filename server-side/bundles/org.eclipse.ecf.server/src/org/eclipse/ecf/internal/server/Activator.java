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

package org.eclipse.ecf.internal.server;

import java.net.URL;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.server.Config;
import org.eclipse.ecf.server.ECFTCPServerStartup;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends Plugin {

	//The shared instance.
	private static Activator plugin;
	
	ECFTCPServerStartup servers;
	
	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		startServers(context.getBundle().getEntry(Config.serverconfigfile));
	}

	private void startServers(URL anURL) {
		if (servers == null) {
			try {
				if (anURL != null) servers = new ECFTCPServerStartup(anURL.openStream());
				else servers = new ECFTCPServerStartup("server.xml"); //$NON-NLS-1$
			} catch (Exception e) {
				Activator.log("Exception starting ecf tcp servers",e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		stopServers();
		plugin = null;
	}

	private void stopServers() {
		if (servers != null) {
			servers.destroyServers();
			servers = null;
		}
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(String message) {
		getDefault().getLog().log(
				new Status(IStatus.OK, Activator.getDefault().getBundle().getSymbolicName(), IStatus.OK, message, null));
	}
	public static void log(String message, Throwable e) {
		getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(), IStatus.OK,
						message, e));
	}

}
