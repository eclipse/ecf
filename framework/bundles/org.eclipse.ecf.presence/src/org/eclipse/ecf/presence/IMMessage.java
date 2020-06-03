/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.identity.ID;

/**
 * Abstract superclass for different types of messages.
 */
public abstract class IMMessage implements IIMMessage {

	private static final long serialVersionUID = 8954529981814747271L;

	protected ID fromID;

	public IMMessage(ID fromID) {
		this.fromID = fromID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.IIMMessage#getFromID()
	 */
	public ID getFromID() {
		return fromID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

}
