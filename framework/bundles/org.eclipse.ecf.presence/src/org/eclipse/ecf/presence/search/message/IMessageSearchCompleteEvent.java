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

package org.eclipse.ecf.presence.search.message;

import org.eclipse.ecf.presence.search.ISearch;

/**
 * 
 * @since 2.0
 *
 */
public interface IMessageSearchCompleteEvent extends IMessageSearchEvent {

	/**
	 * Provide the result for a non-blocking search
	 * @return ISearch
	 */
	public ISearch getSearch();

}
