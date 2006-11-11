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

package org.eclipse.ecf.server;

import org.eclipse.osgi.util.NLS;

public class Config extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.server.config"; //$NON-NLS-1$
	private Config() {
	}
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Config.class);
	}
	public static String serverconfigfile;
}
