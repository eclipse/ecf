/* 
 * Copyright (c) 2009 Siemens Enterprise Communications GmbH & Co. KG, 
 * Germany. All rights reserved.
 *
 * Siemens Enterprise Communications GmbH & Co. KG is a Trademark Licensee 
 * of Siemens AG.
 *
 * This material, including documentation and any related computer programs,
 * is protected by copyright controlled by Siemens Enterprise Communications 
 * GmbH & Co. KG and its licensors. All rights are reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ecf.tests.osgi.services.discovery.local;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public class Activator implements BundleActivator {

	// The shared instance
	private static Activator plugin;

	private BundleContext bc = null;

	/**
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		bc = context;
		// To startup the implementation bundle we try to start it.
		// TODO remove this bad hack.
		boolean startBundle = startBundle("org.eclipse.ecf.osgi.services.discovery.local");
		if(!startBundle) {
			System.err.println("Missing org.eclipse.ecf.osgi.services.discovery.local bundle?");
		}
	}

	/**
	 * @throws BundleException
	 */
	public boolean startBundle(final String symbolicName) throws BundleException {
		Bundle[] bundles = bc.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			if (bundles[i].getSymbolicName().startsWith(symbolicName)) {
				bundles[i].start();
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public BundleContext getBundleContext() {
		return bc;
	}

}
