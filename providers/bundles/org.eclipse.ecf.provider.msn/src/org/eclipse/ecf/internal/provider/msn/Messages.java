/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.msn;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.provider.msn.messages"; //$NON-NLS-1$
	
	public static String MSNContainer_TargetIDNotMSNID;
	
	public static String MSNNamespace_ParameterIsNull;
	public static String MSNNamespace_ParameterIsInvalid;
	
	public static String MSNRosterEntry_Message;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
