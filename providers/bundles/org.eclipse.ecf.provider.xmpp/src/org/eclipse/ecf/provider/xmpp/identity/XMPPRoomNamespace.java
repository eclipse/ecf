/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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
package org.eclipse.ecf.provider.xmpp.identity;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.provider.xmpp.Messages;

public class XMPPRoomNamespace extends Namespace {

	private static final long serialVersionUID = 4348545761410397583L;

	private static final String XMPP_ROOM_PROTOCOL = "xmpp.muc"; //$NON-NLS-1$

	public ID createInstance(Object[] args) throws IDCreateException {
		try {
			if (args.length == 5) {
				return new XMPPRoomID(this, (String) args[0], (String) args[1], (String) args[2], (String) args[3], (String) args[4]);
			}
			throw new IllegalArgumentException(Messages.XMPPRoomNamespace_EXCEPTION_INVALID_ARGUMENTS);
		} catch (final Exception e) {
			throw new IDCreateException(Messages.XMPPRoomNamespace_EXCEPTION_ID_CREAT, e);
		}
	}

	public String getScheme() {
		return XMPP_ROOM_PROTOCOL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedParameterTypesForCreateInstance()
	 */
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] {{String.class, String.class, String.class, String.class, String.class}};
	}
}
