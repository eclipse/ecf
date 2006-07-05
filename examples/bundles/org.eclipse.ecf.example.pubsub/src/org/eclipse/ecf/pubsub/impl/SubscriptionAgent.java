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
package org.eclipse.ecf.pubsub.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.ReplicaSharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.ISharedObjectCreateResponseEvent;
import org.eclipse.ecf.core.events.ISharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.pubsub.IPublishedService;
import org.eclipse.ecf.pubsub.ISubscription;
import org.eclipse.ecf.pubsub.ISubscriptionCallback;

public class SubscriptionAgent extends PlatformObject implements ISharedObject {
	
	protected static final Object CONTAINER_ID_KEY = new Integer(0);
	
	protected static final Object SHARED_OBJECT_ID_KEY = new Integer(1);
	
	protected static final Object CALLBACK_KEY = new Integer(2);

	protected ISharedObjectConfig config;
	
	protected ID containerID;
	
	protected ID sharedObjectID;
	
	protected ISubscriptionCallback callback;
	
	protected boolean subscribed;
	
	protected boolean disposed;
	
	public void init(ISharedObjectConfig config) throws SharedObjectInitException {
		this.config = config;
		Map props = config.getProperties();
		
		if (isPrimary()) { 
			containerID = (ID) props.get(CONTAINER_ID_KEY);
			if (containerID == null)
				throw new SharedObjectInitException("containerID is required");

			callback = (ISubscriptionCallback) props.get(CALLBACK_KEY);
			if (callback == null)
				throw new SharedObjectInitException("callback is required");
		}
		
		sharedObjectID = (ID) props.get(SHARED_OBJECT_ID_KEY);
		if (sharedObjectID == null)
			throw new SharedObjectInitException("sharedObjectID is required");
	}

	public void handleEvent(Event event) {
		if (event instanceof ISharedObjectActivatedEvent) {
			ISharedObjectActivatedEvent e = (ISharedObjectActivatedEvent) event;
			if (e.getActivatedID().equals(config.getSharedObjectID()))
				activated();
			else
				activated(e.getActivatedID());
		} else if (event instanceof ISharedObjectDeactivatedEvent) {
			ISharedObjectDeactivatedEvent e = (ISharedObjectDeactivatedEvent) event;
			if (e.getDeactivatedID().equals(config.getSharedObjectID()))
				deactivated();
		} else if (event instanceof IContainerDisconnectedEvent) {
			IContainerDisconnectedEvent e = (IContainerDisconnectedEvent) event;
			if (e.getTargetID().equals(e.getLocalContainerID()))
				disconnected();
			else
				disconnected(e.getTargetID());
		} else if (event instanceof ISharedObjectCreateResponseEvent)
			received((ISharedObjectCreateResponseEvent) event);
	}
	
	protected boolean isPrimary() {
		return config.getContext().getLocalContainerID().equals(config.getHomeContainerID());
	}
	
	protected void activated() {
		ISharedObjectContext ctx = config.getContext();
		if (isPrimary()) {
			try {
				ctx.sendCreate(containerID, createReplicaDescription());
				// TODO set timer to time out if no response received within some bound
			} catch (IOException e) {
				callback.subscriptionFailed(e);
				ctx.getSharedObjectManager().removeSharedObject(config.getSharedObjectID());
			}

			return;
		}
		
		ISharedObjectManager mgr = ctx.getSharedObjectManager();
		ISharedObject so = mgr.getSharedObject(sharedObjectID);
		try {
			ID homeContainerID = config.getHomeContainerID();
			if (so instanceof IPublishedService) {
				IPublishedService svc = (IPublishedService) so;
				svc.subscribe(homeContainerID);
				subscribed = true;
			} else {
				ctx.sendCreateResponse(homeContainerID, new IllegalArgumentException("Not an IPublishedService."), -1);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			ctx.getSharedObjectManager().removeSharedObject(config.getSharedObjectID());
		}
	}

	protected void activated(ID sharedObjectID) {
		if (isPrimary() && sharedObjectID.equals(this.sharedObjectID))
			callback.subscribed(new Subscription());
	}
	
	protected void deactivated() {
		if (isPrimary()) {
			synchronized (this) {
				disposed = true;
			}

			return;
		}
		
		if (subscribed) {
			ISharedObject so = config.getContext().getSharedObjectManager().getSharedObject(sharedObjectID);
			if (so instanceof IPublishedService) {
				IPublishedService svc = (IPublishedService) so;
				svc.unsubscribe(config.getHomeContainerID());
				subscribed = false;
			}
		}
	}
	
	protected void disconnected() {
		config.getContext().getSharedObjectManager().removeSharedObject(config.getSharedObjectID());
	}
	
	protected void disconnected(ID containerID) {
		if (containerID.equals(config.getHomeContainerID()) || containerID.equals(this.containerID))
			config.getContext().getSharedObjectManager().removeSharedObject(config.getSharedObjectID());
	}
	
	protected void received(ISharedObjectCreateResponseEvent e) {
		if (e.getRemoteContainerID().equals(containerID) && e.getSenderSharedObjectID().equals(config.getSharedObjectID()))
			callback.subscriptionFailed(e.getException());
	}
	
	protected ReplicaSharedObjectDescription createReplicaDescription() {
		Map props = new HashMap(1);
		props.put(SHARED_OBJECT_ID_KEY, sharedObjectID);
		return new ReplicaSharedObjectDescription(getClass(), config.getSharedObjectID(), config.getHomeContainerID(), props);
	}

	public void handleEvents(Event[] events) {
		for (int i = 0; i < events.length; ++i)
			handleEvent(events[i]);
	}
	
	public void dispose(ID containerID) {
		config = null;
	}
	
	protected class Subscription implements ISubscription {

		public ID getID() {
			return sharedObjectID;
		}

		public ID getHomeContainerID() {
			return containerID;
		}

		public void dispose() {
			synchronized (SubscriptionAgent.this) {
				if (disposed)
					return;
				
				disposed = true;
			}
			
			ISharedObjectContext ctx = config.getContext();
			try {
				ctx.sendDispose(containerID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			ctx.getSharedObjectManager().removeSharedObject(config.getSharedObjectID());
		}
	}
}
