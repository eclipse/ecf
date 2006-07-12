/**
 * Copyright (c) 2006 Ecliptical Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ecliptical Software Inc. - initial API and implementation
 */
package org.eclipse.ecf.pubsub.model.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ReplicaSharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.example.pubsub.SerializationUtil;
import org.eclipse.ecf.pubsub.IPublishedService;
import org.eclipse.ecf.pubsub.model.IMasterModel;

public class LocalAgent extends AgentBase implements IPublishedService, IMasterModel {
	
	protected Map subscriptions;
	
	private final Object subscriptionMutex = new Object();
	
	protected void initializeData(Object data) throws SharedObjectInitException {
		this.data = data;
	}
	
	public synchronized void update(Object data) throws IOException {
		config.getContext().sendMessage(null, SerializationUtil.serialize(data));
	}
	
	public Map getProperties() {
		return Collections.EMPTY_MAP;
	}
	
	public void subscribe(ID containerID) {
		synchronized (subscriptionMutex) {
			if (subscriptions == null)
				subscriptions = new HashMap();
			
			Integer refCount = (Integer) subscriptions.get(containerID);
			if (refCount == null)
				refCount = new Integer(0);
			
			refCount = new Integer(refCount.intValue() + 1);
			subscriptions.put(containerID, refCount);
		}
		
		ISharedObjectContext ctx = config.getContext();
		try {
			ctx.sendCreate(containerID, createRemoteAgentDescription());
		} catch (IOException e) {
			// TODO Log me!
			e.printStackTrace();
		}
	}
	
	public void unsubscribe(ID containerID) {
		boolean disposeReplica = false;
		synchronized (subscriptionMutex) {
			if (subscriptions != null) {
				Integer refCount = (Integer) subscriptions.get(containerID);
				if (refCount != null) {
					refCount = new Integer(refCount.intValue() - 1);
					if (refCount.intValue() <= 0) {
						subscriptions.remove(containerID);
						disposeReplica = true;
					} else {
						subscriptions.put(containerID, refCount);
					}
				}
			}
		}
		
		if (disposeReplica)
			try {
				config.getContext().sendDispose(containerID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	protected void deactivated() {
		if (isConnected())
			try {
				config.getContext().sendDispose(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	protected ReplicaSharedObjectDescription createRemoteAgentDescription() {
		Map props = new HashMap(2);
		try {
			props.put(INITIAL_DATA_KEY, SerializationUtil.serialize(data));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		props.put(MODEL_UPDATER_KEY, updaterID);
		return new ReplicaSharedObjectDescription(RemoteAgent.class, config.getSharedObjectID(), config.getHomeContainerID(), props);
	}
}
