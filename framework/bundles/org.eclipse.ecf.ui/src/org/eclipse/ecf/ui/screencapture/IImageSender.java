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

package org.eclipse.ecf.ui.screencapture;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.swt.graphics.ImageData;

/**
 * Interface for sending image (represented by ImageData) to target receiver.
 */
public interface IImageSender {

	/**
	 * Send imageData to targetID.
	 * 
	 * @param targetID the target to send the image to.  May be <code>null</code>.
	 * @param imageData the imageData to send.  May not be <code>null</code>.
	 */
	public void sendImage(ID targetID, ImageData imageData);

}
