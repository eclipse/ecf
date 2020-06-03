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
 * A representation of a query criterion for a restriction in a Criteria.
 * A Criteria is formed by one or several criterion, 
 * that together are able to compose an entire expression to project 
 * a result that match the criteria. The instances of this should be 
 * created for {@link IRestriction} implementations.
 * @since 2.0
 */
public interface ICriterion {

	/**
	 * Returns a expression composed for the search.
	 * The String can be something like 'field' + 'operator' + value, 
	 * that it will be interpreted for each specific provider.
	 * @return String
	 */
	public String toExpression();
}
