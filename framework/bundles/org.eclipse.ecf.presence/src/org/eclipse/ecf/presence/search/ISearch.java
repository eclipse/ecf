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

/**
 * Provide access for the list of matched query results. This interface can be
 * extended to supply more features around the result list.
 * @since 2.0
 */
public interface ISearch {

	/**
	 * Gets a result list.
	 * Will not return <code>null</code>.
	 * @return IResultList list of {@link IResult}. Will not be <code>null</code>
	 */
	IResultList getResultList();

	/**
	 * 
	 * @param resultList
	 */
	public void setResultList(IResultList resultList);

}
