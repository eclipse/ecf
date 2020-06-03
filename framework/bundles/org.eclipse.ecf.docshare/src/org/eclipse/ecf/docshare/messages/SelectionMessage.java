/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
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

package org.eclipse.ecf.docshare.messages;

/**
 * @since 2.1
 *
 */
public class SelectionMessage extends Message {

	private static final long serialVersionUID = 6451633617366707234L;

	int offset;
	int length;

	public SelectionMessage(int offset, int length) {
		super();
		this.offset = offset;
		this.length = length;
	}

	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

}
