/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.msn;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.provider.msn.messages"; //$NON-NLS-1$
	
	public static String MSNNamespace_ParameterIsNull;
	public static String MSNNamespace_ParameterIsInvalid;
	
	public static String MSNRosterEntry_Message;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
