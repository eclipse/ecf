/*******************************************************************************
 * Copyright (c) 2008 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.remoteservices.ui;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.remoteservices.ui.messages"; //$NON-NLS-1$

	public static String MethodInvocationDialog_ShellTitle;
	public static String MethodInvocationDialog_AvailableMethodsLabel;
	public static String MethodInvocationDialog_ArgumentsLabel;
	public static String MethodInvocationDialog_ParameterColumn;
	public static String MethodInvocationDialog_ValueColumn;
	public static String MethodInvocationDialog_TimeoutLabel;
	public static String MethodInvocationDialog_InvocationTypeLabel;
	public static String MethodInvocationDialog_InvocationTypeAsyncListener;
	public static String MethodInvocationDialog_InvocationTypeAsyncFutureResult;
	public static String MethodInvocationDialog_InvocationTypeAsyncFireAndGo;
	public static String MethodInvocationDialog_InvocationTypeOSGiServiceProxy;
	public static String MethodInvocationDialog_InvocationTypeRemoteServiceProxy;
	public static String MethodInvocationDialog_InvocationTypeSynchronous;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
