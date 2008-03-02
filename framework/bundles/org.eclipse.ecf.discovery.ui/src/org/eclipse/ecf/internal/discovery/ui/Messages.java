/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.discovery.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.discovery.ui.messages"; //$NON-NLS-1$

	public static String DiscoveryView_ERROR_SHOW_VIEW_MESSAGE;

	public static String DiscoveryView_ERROR_SHOW_VIEW_TITLE;

	public static String AbstractRemoteServiceAccessHandler_DISCONNECT_MENU_TEXT;

	public static String AbstractRemoteServiceAccessHandler_MSG_BOX_RECEIVED_EXCEPTION_TEXT;

	public static String AbstractRemoteServiceAccessHandler_MSG_BOX_RECEIVED_EXCEPTION_TITLE;

	public static String AbstractRemoteServiceAccessHandler_MSG_BOX_RECEIVED_RESP_TEXT;

	public static String AbstractRemoteServiceAccessHandler_MSG_BOX_RECEIVED_RESP_TITLE;

	public static String AbstractRemoteServiceAccessHandler_NOT_AVAILABLE_MENU_TEXT;

	public static String DiscoveryView_Services;
	public static String DiscoveryView_REFRESH_ACTION_LABEL;

	public static String DiscoveryView_REFRESH_SERVICES_TOOLTIPTEXT;

	public static String DiscoveryView_EXCEPTION_CREATING_SERVICEACCESSHANDLER;

	public static String DiscoveryView_NO_SERVICE_HANDLER_LABEL;

	public static String HttpServiceAccessHandler_EXCEPTION_CREATEBROWSER;

	public static String HttpServiceAccessHandler_MENU_TEXT;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

}
