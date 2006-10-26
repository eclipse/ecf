/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import org.eclipse.ecf.core.events.IContainerEvent;

/**
 * Listener for objects that wish to receive events from an IContainer
 * instances.
 * 
 * @see IContainer#addListener(IContainerListener, String)
 */
public interface IContainerListener {
	/**
	 * Handle event from IContainer
	 * 
	 * @param event
	 */
	public void handleEvent(IContainerEvent event);
}