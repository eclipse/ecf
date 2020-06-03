/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.sharedobject.events;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

/**
 * Shared object event
 * 
 */
public interface ISharedObjectEvent extends Event {

	/**
	 * Get ID of sender shared object responsible for this event
	 * 
	 * @return ID of sender shared object. Will not be null.
	 */
	public ID getSenderSharedObjectID();

	/**
	 * Get the Event from the sender shared object
	 * 
	 * @return Event the event in question
	 */
	public Event getEvent();
}