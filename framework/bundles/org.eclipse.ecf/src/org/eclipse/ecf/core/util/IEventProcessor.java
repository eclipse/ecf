/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

public interface IEventProcessor {
	/**
	 * Accept given Event. If implementers return true for a given Event then
	 * this indicates that the {@link #processEvent(Event) } method should be
	 * subsequently called.
	 * 
	 * @param event
	 *            the Event under consideration for acceptance
	 * @return true if given Event should be passed to processEvent method,
	 *         false if it should not
	 */
	public boolean acceptEvent(Event event);
	/**
	 * Process given Event
	 * 
	 * @param event
	 *            the Event to process
	 * @return Event to be provided to next IEventProcessor in chain. If null,
	 *         this signifies that processing of this Event should cease with
	 *         this Event processor
	 */
	public Event processEvent(Event event);
}