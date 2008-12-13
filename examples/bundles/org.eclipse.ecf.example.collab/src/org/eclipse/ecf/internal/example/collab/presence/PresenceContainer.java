/******************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.example.collab.presence;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.example.collab.ClientPlugin;
import org.eclipse.ecf.presence.*;
import org.eclipse.ecf.presence.im.IChatManager;
import org.eclipse.ecf.presence.roster.IRosterManager;
import org.eclipse.ecf.presence.service.IPresenceService;
import org.osgi.framework.BundleContext;

public class PresenceContainer extends AbstractPresenceContainer implements IPresenceSender, IPresenceService {

	private final IContainer container;
	private final IRosterManager manager;

	public PresenceContainer(IContainer container, IUser user) {
		this.container = container;
		manager = new RosterManager(this, user);

		BundleContext bundleContext = ClientPlugin.getDefault().getBundle().getBundleContext();
		bundleContext.registerService(IPresenceService.class.getName(), this, null);
	}

	public IChatManager getChatManager() {
		return null;
	}

	public IRosterManager getRosterManager() {
		return manager;
	}

	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		} else if (adapter == IContainer.class) {
			return container;
		}
		return super.getAdapter(adapter);
	}

	public void addMessageListener(IIMMessageListener listener) {
		// unimplemented because messages do not currently go through the presence container
	}

	public void removeMessageListener(IIMMessageListener listener) {
		// unimplemented because messages do not currently go through the presence container
	}

	public void sendPresenceUpdate(ID targetId, IPresence presence) throws ECFException {
		// unimplemented as we have no concept of presence support, either online or offline
	}

}
