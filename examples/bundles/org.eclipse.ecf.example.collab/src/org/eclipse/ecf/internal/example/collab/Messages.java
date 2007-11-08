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
	public static String EditorCompoundContributionItem_EXCEPTION_NOT_CONNECTED_MESSAGE;
	public static String EditorCompoundContributionItem_EXCEPTION_NOT_CONNECTED_TITLE;
	public static String EditorCompoundContributionItem_SHARE_SELECTION_MENU_ITEM_NAME;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
