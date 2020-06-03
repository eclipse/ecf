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
 * Item conflict structure
 * 
 */
public interface IConflict extends IUpdateInfo {
	/**
	 * Get version number for conflict.
	 * 
	 * @return Integer version number. Minimum of 1.
	 */
	public Integer getVersion();
}
