/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.presence.im;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;

/**
 * A single chat line from history.
 */
public interface IHistoryLine extends IAdaptable {

	/**
	 * Get the Date this history line was sent or received.  If {@link #isIncoming()}
	 * is true, it is the Date that the message was received.  If false, it is the
	 * Date the message was sent.
	 * 
	 * @return Date associated with this history line.  Will not be <code>null</code>.
	 */
	public Date getDate();
	/**
	 * 
	 * @return <code>true</code> if history line was incoming (we received it), <code>false</code> if we sent it.
	 */
	public boolean isIncoming();
	/**
	 * Get the actual text of the line.
	 * 
	 * @return String text of the message.
	 */
	public String getText();
}
