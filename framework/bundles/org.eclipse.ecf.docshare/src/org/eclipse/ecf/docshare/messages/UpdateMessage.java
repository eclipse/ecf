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

package org.eclipse.ecf.docshare.messages;

/**
 *
 */
public class UpdateMessage extends Message {

	private static final long serialVersionUID = -3195542805471664496L;

	String text;
	int offset;
	int length;

	public UpdateMessage(int offset, int length, String text) {
		this.offset = offset;
		this.length = length;
		this.text = text;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public String getText() {
		return text;
	}
}
