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

package org.eclipse.ecf.internal.example.collab;

import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.example.collab.messages"; //$NON-NLS-1$
	public static String EclipseCollabHyperlink_EXCEPTION_OPEN_EDITOR;
	public static String EclipseCollabHyperlink_EXCEPTION_OPEN_EDITOR_TITLE;
	public static String EclipseCollabHyperlink_MESSAGE_EXCEPTION_OPEN_EDITOR;
	public static String EclipseCollabSharedObject_CANNOT_OPEN_EDITOR_MESSAGE;
	public static String EclipseCollabSharedObject_CANNOT_OPEN_EDITOR_TITLE;
	public static String EclipseCollabSharedObject_DIALOG_OPEN_SHARED_EDITOR_TEXT;
	public static String EclipseCollabSharedObject_FILE_TRANSFER_RECEIVED;
	public static String EclipseCollabSharedObject_FILE_TRANSFER_RECEIVING;
	public static String EclipseCollabSharedObject_MARKER_NAME;
	public static String EclipseCollabSharedObject_OPEN_SHARED_EDITOR_QUESTION;
	public static String EclipseCollabSharedObject_PRIVATE_MESSAGE_TEXT;
	public static String EclipseCollabSharedObject_PROJECT_NAME;
	public static String EclipseCollabSharedObject_SCREEN_CAPTURE_FROM;
	public static String EclipseCollabSharedObject_TITLE_BAR;
	public static String EclipseCollabSharedObject_TREE_TOP_LABEL;
	public static String EclipseCollabSharedObject_UNKNOWN_USERNAME;
	public static String EclipseCollabSharedObject_WINDOW_TITLE;
	public static String EclipseCollabSharedObject_WORKSPACE_RESOURCE_NAME;
	public static String EditorCompoundContributionItem_EXCEPTION_NOT_CONNECTED_MESSAGE;
	public static String EditorCompoundContributionItem_EXCEPTION_NOT_CONNECTED_TITLE;
	public static String EditorCompoundContributionItem_SHARE_SELECTION_MENU_ITEM_NAME;
	public static String SharedObjectMsg_EXCEPTION_METHOD_NOT_NULL;
	public static String SharedObjectMsg_EXCEPTION_NOT_SERIALIZABLE;
	public static String SharedObjectMsg_EXCEPTION_NULL_TARGET;
	public static String TransactionSharedObject_EXCEPTION_FROM_ABORT;
	public static String TransactionSharedObject_EXCEPTION_INTERUPTED;
	public static String TransactionSharedObject_EXCEPTION_ON_COMMIT_MESSAGE;
	public static String TransactionSharedObject_EXCEPTION_TIMEOUT;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
