/****************************************************************************
 * Copyright (c) 2008 Marcelo Mayworm.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: 	Marcelo Mayworm - initial API and implementation
 * 
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence.search;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.user.IUser;

/**
 * Each result returned for the search will be reach through this interface.
 * The result contain the user that match the search.
 * @since 2.0
 */
public interface IResult extends IAdaptable {

	/**
	 * Get user for the search. This is the user that comes from the search.
	 * 
	 * @return IUser user associated with the search. Will not be <code>null</code>
	 */
	public IUser getUser();
}
