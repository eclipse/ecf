/*******************************************************************************
 * Copyright (c) 2020 Paul Verest, Benjamin Gurok, Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Apache Public License v2.0 which 
 * accompanies this distribution, and is available at 
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Contributors: Paul Verest, Benjamin Gurok, and Composent, Inc. - initial 
 * API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservices.tooling.bndtools.editors;

import org.eclipse.swt.graphics.RGB;

/*
 * @author Benjamin gurok
 * @author Paul Verest
 */
public class Constants {
	
	//public static final String PREFERENCES_PAGE = "org.nodeclipse.ui.preferences.NodePreferencePage";

	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.grpc.proto.editor";
	public static final String ICON_PATH = "icons/proto_16x16.png";
	
	public static final String DESCRIPTION = "Minimalist .proto files Editor";
	
	public static final String KEY_COLOR_COMMENT = "color_comment";
	public static final String KEY_COLOR_DOC = "color_doc";
	public static final String KEY_COLOR_KEYWORD = "color_keyword";
	public static final String KEY_COLOR_STRING = "color_string";
	public static final String KEY_COLOR_NUMBER = "color_number";
	public static final String KEY_COLOR_NORMAL = "color_normal";
	
	public static final String KEY_BOLD_KEYWORD = "bold_keyword";
	
	public static final RGB DEFAULT_COLOR_COMMENT = new RGB(63, 127, 95);
	public static final RGB DEFAULT_COLOR_DOC = new RGB(127, 127, 159);
	public static final RGB DEFAULT_COLOR_KEYWORD = new RGB(127, 0, 85);
	public static final RGB DEFAULT_COLOR_STRING = new RGB(42, 0, 255);
	public static final RGB DEFAULT_COLOR_NUMBER = new RGB(0, 0, 0);
	public static final RGB DEFAULT_COLOR_NORMAL = new RGB(0, 0, 0);

}	
