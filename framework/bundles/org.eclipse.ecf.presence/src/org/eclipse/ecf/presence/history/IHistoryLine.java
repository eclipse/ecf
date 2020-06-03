/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.presence.history;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.ID;

/**
 * A single chat line from history.
 */
public interface IHistoryLine extends IAdaptable {

	/**
	 * Get ID of sender for this history line.  ID returned will not be <code>null</code>.
	 * 
	 * @return ID of sender for this history line.  Will not be <code>null</code>.
	 */
	public ID getSenderID();
	
	/**
	 * Get ID of receiver for this history line.  ID returned will not be <code>null</code>.
	 * 
	 * @return ID of receiver for this history line.  Will not be <code>null</code>.
	 */
	public ID getReceiverID();
	
	/**
	 * Get the Date this history line was sent or received.  
	 * 
	 * @return Date associated with this history line.  Will not be <code>null</code>.
	 */
	public Date getDate();
	/**
	 * Get the actual text of the line.
	 * 
	 * @return String text of the message.
	 */
	public String getText();
	
}
