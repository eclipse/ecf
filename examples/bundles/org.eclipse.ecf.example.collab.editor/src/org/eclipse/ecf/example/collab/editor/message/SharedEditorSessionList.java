/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.example.collab.editor.message;

import java.util.List;

/**
 * This message is passed as a response to a <code>SharedEditorSessionListRequest</code> message.
 * The message is passed from peers that have open editor sessions to all others in the shared container.
 * 
 * @author kgilmer
 *
 */
public class SharedEditorSessionList extends AbstractMessage {
	private static final long serialVersionUID = 4337027955521207775L;
	private List sessionNames;
	
	public SharedEditorSessionList(List names) {
		sessionNames = names;
	}
	
	public List getNames() {
		return sessionNames;
	}
}
