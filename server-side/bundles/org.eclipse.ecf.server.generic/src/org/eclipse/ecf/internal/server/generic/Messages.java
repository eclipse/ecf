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

package org.eclipse.ecf.internal.server.generic;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.server.generic.messages"; //$NON-NLS-1$
	public static String Activator_SERVER_XML;
	public static String ServerManager_EXCEPTION_DISCOVERY_REGISTRATION;

	public static String ServerStarter_EXCEPTION_CREATING_SERVER;
	public static String ServerStarter_EXCEPTION_DISPOSING_SERVER;
	public static String ServerStarter_STARTING_SERVER;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// private null constructor
	}
}
