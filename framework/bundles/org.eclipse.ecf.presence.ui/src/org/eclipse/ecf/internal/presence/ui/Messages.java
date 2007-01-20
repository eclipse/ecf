/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.presence.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.presence.ui.messages"; //$NON-NLS-1$
	
	public static String MultiRosterView_ShowOffline;
	
	public static String RosterWorkbenchAdapterFactory_Mode;
	public static String RosterWorkbenchAdapterFactory_Type;
	public static String RosterWorkbenchAdapterFactory_Account;
	public static String RosterWorkbenchAdapterFactory_Disconnected;
	public static String RosterWorkbenchAdapterFactory_GroupLabel;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
