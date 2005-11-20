/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery;

/**
 * Listener for receiving service events
 * 
 */
public interface IServiceListener {
	/**
	 * Notification that a service has been added.
	 * 
	 * @param event
	 */
	public void serviceAdded(IServiceEvent event);
	/**
	 * Notification that a service has been removed.
	 * 
	 * @param event
	 */
	public void serviceRemoved(IServiceEvent event);
	/**
	 * Notification that a service has been resolved (that complete service info is now available).
	 * 
	 * @param event
	 */
	public void serviceResolved(IServiceEvent event);
}
