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
package org.eclipse.ecf.presence.im;

/**
 * Adapter interface for chatIDs. The typical usage of this adapter is as
 * follows:
 * 
 * <pre>
 *      ID myID = ...
 *      IChatID chatID = (IChatID) myID.getAdapter(IChatID.class);
 *      if (chatID != null) {
 *        ...use chatID here
 *      }
 * </pre>
 * 
 */
public interface IChatID {
	/**
	 * Get username for this IChatID
	 * 
	 * @return String username for the implementing IChatID. May return
	 *         <code>null</code>.
	 */
	public String getUsername();
	
	/**
	 * Get hostname for this IChatID
	 * 
	 * @return 	String hostname for the implementing IChatID. May return
	 *         <code>null</code>.
	 */
	public String getHostname();
}
