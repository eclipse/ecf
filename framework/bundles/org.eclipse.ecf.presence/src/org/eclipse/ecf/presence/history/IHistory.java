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

package org.eclipse.ecf.presence.history;

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Chat history information.
 */
public interface IHistory extends IAdaptable {
	
	/**
	 * Get the history lines between the given start and end dates (inclusive).
	 * 
	 * @param start the start Date to retrieve history lines for.  If <code>null</code>, all
	 * history for the partner associated with this history is provided.  If not 
	 * <code>null</code>, then all history with date equal to or after the start
	 * date will be provided.
	 * 
	 * @param end the end Date to retrieve history lines for.  If <code>null</code>, all
	 * history for the partner associated with this history is provided.  If not <code>null</code>,
	 * then all history with date equal to or prior the end date will be provided.
	 * 
	 * @return IHistoryLine[] history lines between start and end date.  Will return empty
	 * array if no history lines matchinq query exist.  Will not return <code>null</code>.
	 */
	public IHistoryLine[] getHistoryLines(Date start, Date end);
	
}
