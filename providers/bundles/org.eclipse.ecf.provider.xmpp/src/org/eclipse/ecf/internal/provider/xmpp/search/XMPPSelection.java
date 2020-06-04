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

import org.eclipse.ecf.presence.search.ICriterion;
import org.eclipse.ecf.presence.search.Restriction;

/**
 * Implement a specific Selection for XMPP
 * @since 3.0
 */
public class XMPPSelection extends Restriction {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ecf.presence.search.ISelection#eq(java.lang.String, java.lang.String)
	 */
	public ICriterion eq(String field, String value) {
		//the operator is ignored for XMPP
		return new XMPPSimpleCriterion(field, value, "");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ecf.presence.search.ISelection#eq(java.lang.String, java.lang.String)
	 */
	public ICriterion eq(String field, String value, boolean ignoreCase) {
		//the operator is ignored for XMPP
		return new XMPPSimpleCriterion(field, value, "");
	}
	
}
