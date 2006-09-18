/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import java.util.Map;

import org.eclipse.ecf.core.identity.ID;

public abstract class AbstractSharedObjectContainerAdapter {

	protected ISharedObjectManager sharedObjectManager;

	protected ID delegateID;

	protected AbstractSharedObjectContainerAdapter() {

	}

	protected AbstractSharedObjectContainerAdapter(ISharedObjectManager manager) {
		this.sharedObjectManager = manager;
		this.delegateID = addDelegate();
	}

	protected ISharedObjectManager getSharedObjectManager() {
		return sharedObjectManager;
	}

	protected ID addDelegate() {
		try {
			return getSharedObjectManager().addSharedObject(
					createSharedObjectID(), createSharedObjectInstance(),
					createSharedObjectProperties());
		} catch (SharedObjectAddException e) {
			throw new RuntimeException(
					"Exception adding shared object instance", e);
		}
	}

	protected abstract ISharedObject createSharedObjectInstance();

	protected abstract ID createSharedObjectID();

	protected Map createSharedObjectProperties() {
		return null;
	}

	public Object getDelegate() {
		return getSharedObjectManager().getSharedObject(delegateID);
	}

}
