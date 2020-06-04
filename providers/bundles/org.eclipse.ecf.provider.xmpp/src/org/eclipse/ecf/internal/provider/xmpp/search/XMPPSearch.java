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

import org.eclipse.ecf.presence.search.ICriteria;
import org.eclipse.ecf.presence.search.IResultList;
import org.eclipse.ecf.presence.search.ISearch;
import org.eclipse.ecf.presence.search.ResultList;

/**
 * Implement ISearch for XMPP
 *@since 3.0
 */
public class XMPPSearch implements ISearch {

	protected IResultList resultList;
	private ICriteria criteria;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ecf.presence.search.ISearch#getResultList()
	 */
	public IResultList getResultList() {
		return resultList;
	}

	protected XMPPSearch(ICriteria criteria) {
		this.criteria = criteria;
	}

	public XMPPSearch(ResultList resultList) {
		this.resultList = resultList;
	}

	public ICriteria getCriteria() {
		return this.criteria;
	}

	public void setResultList(IResultList resultList) {
		this.resultList = resultList;
		
	}
	

	public String toString() {
		StringBuffer sb = new StringBuffer("XMPPSearch["); //$NON-NLS-1$
		sb.append("criteria=").append(getCriteria()).append(";resultlist=").append(getResultList()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return sb.toString();
	}
}
