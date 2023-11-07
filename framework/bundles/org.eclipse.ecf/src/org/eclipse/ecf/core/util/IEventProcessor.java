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
package org.eclipse.ecf.core.util;

/**
 * Event processor for processing events in a sequence.  If the implementer of this interface
 * intends to prevent further processing for the given event, then it should return true
 * to prevent further processing.  It should return false to allow further processing of 
 * the given event to continue (e.g. in a chain of event processors)
 */
public interface IEventProcessor {
	/**
	 * Process given Event
	 * 
	 * @param event
	 *            the Event to process
	 * @return true if the event has been successfully processed and no further
	 *         processing should occur. False if the event should receive
	 *         further processing by another event processor (e.g. in a chain)
	 */
	public boolean processEvent(Event event);
}