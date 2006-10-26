/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

/**
 * Generic event handler interface
 * 
 */
public interface IEventHandler {
	/**
	 * Handle Event passed to this IEventHandler.
	 * 
	 * @param event
	 *            the Event for the implementer to handle
	 */
	public void handleEvent(Event event);

	/**
	 * Handle Events passed to this IEventHandler.
	 * 
	 * @param events
	 *            the Events [] for the implementer to handle
	 */
	public void handleEvents(Event[] events);
}
