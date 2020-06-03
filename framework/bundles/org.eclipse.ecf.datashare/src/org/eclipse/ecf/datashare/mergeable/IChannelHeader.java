/****************************************************************************
 * Copyright (c) 2004 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.datashare.mergeable;

/**
 * Channel header information
 * 
 */
public interface IChannelHeader {
	/**
	 * Get title
	 * 
	 * @return String title. May be <code>null</code>.
	 */
	public String getTitle();

	/**
	 * Get link
	 * 
	 * @return String link. May be <code>null</code>.
	 */
	public String getLink();

	/**
	 * Get description
	 * 
	 * @return String description. May be <code>null</code>.
	 */
	public String getDescription();
}
