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

package org.eclipse.ecf.internal.presence.collab.ui;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.presence.collab.ui.messages"; //$NON-NLS-1$
	public static String URLShare_ENTER_URL_DEFAULT_URL;
	public static String URLShare_ENTER_URL_DIALOG_TEXT;
	public static String URLShare_ERROR_BROWSER_MESSAGE;
	public static String URLShare_ERROR_BROWSER_TITLE;
	public static String Share_ERROR_RECEIVE_MESSAGE;
	public static String Share_ERROR_RECEIVE_TITLE;
	public static String Share_ERROR_SEND_MESSAGE;
	public static String Share_ERROR_SEND_TITLE;
	public static String URLShare_EXCEPTION_LOG_BROWSER;
	public static String Share_EXCEPTION_LOG_MESSAGE;
	public static String Share_EXCEPTION_LOG_SEND;
	public static String URLShare_INPUT_URL_DIALOG_TITLE;
	public static String URLShare_RECEIVED_URL_MESSAGE;
	public static String URLShare_RECEIVED_URL_TITLE;
	public static String URLShareRosterContributionItem_ADD_URL_SHARE_MENU_TEXT;
	public static String URLShareRosterContributionItem_BROWSER_ICON;
	public static String URLShareRosterContributionItem_REMOVE_URL_SHARE_MENU_TEXT;
	public static String URLShareRosterEntryContributionItem_SEND_URL_MENU_TEXT;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
