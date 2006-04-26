/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.example.collab.editor.message;


/**
 * This message is passed when a peer wishes to "discover" all available
 * shared editing sessions in it's shared container group (as defined by the 
 * ChannelID).  An asynchronous <code>SharedEditorSessionList</code> message
 * is expected in return.
 * 
 * @author kgilmer
 *
 */
public class SharedEditorSessionListRequest extends AbstractMessage {
	private static final long serialVersionUID = 2096909220585200273L;

	
}
