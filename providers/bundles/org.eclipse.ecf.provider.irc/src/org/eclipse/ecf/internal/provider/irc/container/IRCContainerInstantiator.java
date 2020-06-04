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
package org.eclipse.ecf.internal.provider.irc.container;

import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.internal.provider.irc.Messages;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainerOptionsAdapter;
import org.eclipse.ecf.presence.chatroom.IChatRoomManager;

public class IRCContainerInstantiator implements IContainerInstantiator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org
	 * .eclipse.ecf.core.ContainerTypeDescription, java.lang.Object[])
	 */
	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		try {
			return new IRCRootContainer(IDFactory.getDefault().createGUID());
		} catch (IDCreateException e) {
			throw new ContainerCreateException(
					Messages.IRCContainerInstantiator_Exception_CreateID_Failed,
					e);
		}
	}

	public String[] getSupportedAdapterTypes(
			ContainerTypeDescription description) {
		return new String[] { IChatRoomManager.class.getName(),
				IChatRoomContainerOptionsAdapter.class.getName(),
				IPresenceContainerAdapter.class.getName() };
	}

	public Class[][] getSupportedParameterTypes(
			ContainerTypeDescription description) {
		return new Class[0][0];
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return null;
	}
}
