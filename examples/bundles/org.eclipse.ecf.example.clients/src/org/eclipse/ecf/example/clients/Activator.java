/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.example.clients;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	private static Activator instance = null;

	public static final int CLIENT_ERROR_CODE = 121;

	public static final String PLUGIN_ID = "org.eclipse.ecf.example.clients"; //$NON-NLS-1$

	public Activator() {
		super();
		instance = this;
	}

	public static Activator getDefault() {
		return instance;
	}

	public void log(int status, String message, Throwable exception) {
		getLog().log(new Status(status, PLUGIN_ID, CLIENT_ERROR_CODE, message, exception));
	}

	/**
	 * This method is called upon plug-in activation
	 * @param context 
	 * @throws Exception 
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 * @param context 
	 * @throws Exception 
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		instance = null;
	}

}
