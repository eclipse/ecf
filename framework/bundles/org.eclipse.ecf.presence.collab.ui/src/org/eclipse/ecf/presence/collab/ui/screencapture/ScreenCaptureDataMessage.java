/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
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

package org.eclipse.ecf.presence.collab.ui.screencapture;

import java.io.Serializable;

import org.eclipse.ecf.core.identity.ID;

/**
 *
 */
public class ScreenCaptureDataMessage implements Serializable {

	private static final long serialVersionUID = 6036044167371073951L;
	ID senderID;
	byte[] data;
	Boolean isDone;

	public ScreenCaptureDataMessage(ID senderID, byte[] data, Boolean isDone) {
		this.senderID = senderID;
		this.data = data;
		this.isDone = isDone;
	}

	/**
	 * @return the senderID
	 */
	public ID getSenderID() {
		return senderID;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return the isDone
	 */
	public Boolean getIsDone() {
		return isDone;
	}

}
