/****************************************************************************
 * Copyright (c) 2006 Ecliptical Software Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Ecliptical Software Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.pubsub.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.sharedobject.ISharedObjectManager;
import org.eclipse.ecf.core.sharedobject.SharedObjectCreateException;
import org.eclipse.ecf.core.sharedobject.SharedObjectDescription;
import org.eclipse.ecf.pubsub.IPublishedServiceRequestor;
import org.eclipse.ecf.pubsub.ISubscriptionCallback;

public class ServiceRequestor implements IPublishedServiceRequestor {
	
	protected final ISharedObjectManager mgr;
	
	public ServiceRequestor(ISharedObjectManager mgr) {
		this.mgr = mgr;
	}

	public void subscribe(ID containerID, ID sharedObjectID, ISubscriptionCallback callback) {
		if (containerID == null || sharedObjectID == null || callback == null)
			throw new IllegalArgumentException();
		
		try {
			mgr.createSharedObject(createSubscriptionDescription(containerID, sharedObjectID, callback));
		} catch (SharedObjectCreateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected SharedObjectDescription createSubscriptionDescription(ID containerID, ID sharedObjectID, ISubscriptionCallback callback) {
		Map props = new HashMap(3);
		props.put(SubscriptionAgent.CONTAINER_ID_KEY, containerID);
		props.put(SubscriptionAgent.SHARED_OBJECT_ID_KEY, sharedObjectID);
		props.put(SubscriptionAgent.CALLBACK_KEY, callback);
		ID id;
		try {
			id = IDFactory.getDefault().createGUID();
		} catch (IDCreateException e) {
			throw new RuntimeException(e);
		}
		
		return new SharedObjectDescription(SubscriptionAgent.class, id, props);
	}
}
