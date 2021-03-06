/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
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
package org.eclipse.ecf.internal.provider;

public interface ECFProviderDebugOptions {

	public static final String DEBUG = ProviderPlugin.PLUGIN_ID + "/debug"; //$NON-NLS-1$

	public static final String EXCEPTIONS_CATCHING = DEBUG + "/exceptions/catching"; //$NON-NLS-1$

	public static final String EXCEPTIONS_THROWING = DEBUG + "/exceptions/throwing"; //$NON-NLS-1$

	public static final String METHODS_ENTERING = DEBUG + "/methods/entering"; //$NON-NLS-1$

	public static final String METHODS_EXITING = DEBUG + "/methods/exiting"; //$NON-NLS-1$

	public static final String SOCONTAINERGMM = DEBUG + "/gmm"; //$NON-NLS-1$

	public static final String CONNECTION = DEBUG + "/connection"; //$NON-NLS-1$

	public static final String CONTAINER = DEBUG + "/container"; //$NON-NLS-1$
	public static final String SHAREDOBJECTWRAPPER = DEBUG + "/sharedobjectwrapper"; //$NON-NLS-1$
	public static final String SHAREDOBJECTCONTEXT = DEBUG + "/sharedobjectcontext"; //$NON-NLS-1$
	public static final String SHAREDOBJECTMANAGER = DEBUG + "/sharedobjectmanager"; //$NON-NLS-1$
	public static final String CONTAINERFACTORY = DEBUG + "/containerfactory"; //$NON-NLS-1$
}
