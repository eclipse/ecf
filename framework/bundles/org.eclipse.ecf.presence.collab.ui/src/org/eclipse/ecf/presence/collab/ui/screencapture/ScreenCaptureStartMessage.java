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
import org.eclipse.ecf.ui.screencapture.ImageWrapper;

/**
 *
 */
public class ScreenCaptureStartMessage implements Serializable {

	private static final long serialVersionUID = 8305404092939645129L;

	ID senderID;
	String senderUser;
	ImageWrapper imageWrapper;

	public ScreenCaptureStartMessage(ID senderID, String senderUser, ImageWrapper imageWrapper) {
		this.senderID = senderID;
		this.senderUser = senderUser;
		this.imageWrapper = imageWrapper;
	}

	public ID getSenderID() {
		return senderID;
	}

	public String getSenderUser() {
		return senderUser;
	}

	public ImageWrapper getImageWrapper() {
		return imageWrapper;
	}
}
