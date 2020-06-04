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
package org.eclipse.ecf.internal.provider.xmpp.search;

import org.eclipse.ecf.presence.search.SimpleCriterion;

/**
 * Implement specific for ICriterion
 * @since 3.0
 */
public class XMPPSimpleCriterion extends SimpleCriterion {

	public XMPPSimpleCriterion(String field, String value, String operator,
			boolean ignoreCase) {
		super(field, value, operator, ignoreCase);

	}

	public XMPPSimpleCriterion(String field, String value, String operator) {
		super(field, value, operator);
	}
	
	/**
	 * Provide the expression compose just for the value
	 */
	public String toExpression() {
		return value;
	}
}
