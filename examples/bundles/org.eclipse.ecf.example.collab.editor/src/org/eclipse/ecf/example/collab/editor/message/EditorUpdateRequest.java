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
 * A message sent from a joining peer, needing the current editor model.
 * This message should be caught by the creator, and respond with a EditorChangeMessage.
 * @author kg11212
 *
 */
public class EditorUpdateRequest extends AbstractMessage {
	private static final long serialVersionUID = 7307387852689460016L;

}
