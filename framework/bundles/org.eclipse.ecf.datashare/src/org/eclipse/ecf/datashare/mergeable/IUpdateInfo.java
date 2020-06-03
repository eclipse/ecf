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

import java.util.Date;

import org.eclipse.ecf.core.identity.IIdentifiable;

/**
 * Info about when an update was made and by whom.
 */
public interface IUpdateInfo extends IIdentifiable {
	/**
	 * Get date of when update was applied.
	 * 
	 * @return Date the date when applied. Will not be <code>null</code>.
	 */
	public Date getWhen();
}
