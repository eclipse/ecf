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

package org.eclipse.ecf.docshare.messages;

import org.eclipse.ecf.core.identity.ID;

/**
 * @since 2.1
 *
 */
public class StartMessage extends Message {

	private static final long serialVersionUID = 4712028336072890912L;

	private final ID senderID;
	private final ID receiverID;
	private final String fromUsername;
	private final String fileName;
	private final String documentContent;

	public StartMessage(ID senderID, String fromUser, ID receiverID, String content, String file) {
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.fromUsername = fromUser;
		this.fileName = file;
		this.documentContent = content;
	}

	public ID getSenderID() {
		return senderID;
	}

	public ID getReceiverID() {
		return receiverID;
	}

	public String getSenderUsername() {
		return fromUsername;
	}

	public String getFilename() {
		return fileName;
	}

	public String getDocumentContent() {
		return documentContent;
	}

}
