/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ISharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;

public class RosterUserAccount {
	/**
	 * 
	 */
	private final RosterView rosterView;

	ID serviceID;

	IUser user;

	ILocalInputHandler inputHandler;
	
	IContainer container = null;

	IPresenceContainerAdapter presenceContainer;

	ISharedObjectContainer soContainer;

	ISharedObject sharedObject = null;

	public RosterUserAccount(RosterView rosterView, ID serviceID, IUser user,
			ILocalInputHandler handler,
			IContainer container,
			IPresenceContainerAdapter presenceContainer,
			ISharedObjectContainer soContainer) {
		this.rosterView = rosterView;
		this.serviceID = serviceID;
		this.user = user;
		this.inputHandler = handler;
		this.container = container;
		this.presenceContainer = presenceContainer;
		this.soContainer = soContainer;
		this.sharedObject = this.rosterView.createAndAddSharedObjectForAccount(this);
	}

	public ID getServiceID() {
		return serviceID;
	}

	public IUser getUser() {
		return user;
	}

	public ILocalInputHandler getInputHandler() {
		return inputHandler;
	}

	public IContainer getContainer() {
		return container;
	}
	
	public IPresenceContainerAdapter getPresenceContainer() {
		return presenceContainer;
	}

	public ISharedObjectContainer getSOContainer() {
		return soContainer;
	}

	public ISharedObject getSharedObject() {
		return sharedObject;
	}
}