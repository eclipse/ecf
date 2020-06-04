/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server.generic.app;

/**
 * @since 3.0
 */
public class GenericServerJavaApplication extends AbstractGenericServerApplication {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		GenericServerJavaApplication app = new GenericServerJavaApplication();
		app.processArguments(args);
		app.initialize();
		if (app.configURL != null)
			System.out.println("Generic server started with config from " + app.configURL); //$NON-NLS-1$
		else
			System.out.println("Generic server started with id=" + app.serverName); //$NON-NLS-1$
		System.out.println("Ctrl-c to exit"); //$NON-NLS-1$
	}

}
