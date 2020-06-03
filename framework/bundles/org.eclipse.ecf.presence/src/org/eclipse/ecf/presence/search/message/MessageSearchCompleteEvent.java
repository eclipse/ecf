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
public class MessageSearchCompleteEvent implements IMessageSearchCompleteEvent {

	private ISearch search;

	public MessageSearchCompleteEvent(ISearch search) {
		this.search = search;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ecf.presence.search.message.IMessageSearchCompleteEvent#getSearch()
	 */
	public ISearch getSearch() {
		return search;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("MessageSearchCompleteEvent[search="); //$NON-NLS-1$
		sb.append(getSearch()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}

}
