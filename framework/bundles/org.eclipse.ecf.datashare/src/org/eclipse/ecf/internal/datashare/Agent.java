/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.datashare;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.datashare.IPublicationCallback;
import org.eclipse.ecf.datashare.ISharedData;
import org.eclipse.ecf.datashare.IUpdateProvider;
import org.eclipse.ecf.datashare.UpdateProviderRegistry;
import org.eclipse.ecf.datashare.multicast.AbstractMulticaster;
import org.eclipse.ecf.datashare.multicast.Activated;
import org.eclipse.ecf.datashare.multicast.IMessageListener;
import org.eclipse.ecf.datashare.multicast.Version;

/**
 * <p>
 * State chart:
 * </p>
 * <p>
 * 1. DataShareService.publish(Object, ID, IUpdateProvider,
 * IPublicationCallback) -> PUBLISHED
 * </p>
 * 
 * @author pnehrer
 */
public class Agent implements ISharedData, ISharedObject, IMessageListener {

	private Object sharedData;

	private ISharedObjectConfig config;

	private IBootstrap bootstrap;

	private ID newContainerID;

	private AbstractMulticaster sender;

	private IUpdateProvider updateProvider;

	private IPublicationCallback pubCallback;

	/**
	 * Default constructor; necessary for replication.
	 */
	public Agent() {
	}

	/**
	 * Publisher's constructor; fully initializes the instance.
	 * 
	 * @param sharedData
	 * @param bootstrap
	 * @param updateProvider
	 * @param pubCallback
	 */
	public Agent(Object sharedData, IBootstrap bootstrap,
			AbstractMulticaster sender, IUpdateProvider updateProvider,
			IPublicationCallback pubCallback) {
		this.sharedData = sharedData;
		this.bootstrap = bootstrap;
		this.sender = sender;
		this.updateProvider = updateProvider;
		this.pubCallback = pubCallback;
	}

	public synchronized ID getID() {
		return config == null ? null : config.getSharedObjectID();
	}

	public Object getData() {
		return sharedData;
	}

	public synchronized void commit() throws ECFException {
		Object update = updateProvider.createUpdate(this);
		boolean sent = sender.sendMessage(update);
		if (!sent)
			throw new ECFException("Commit failed.");
	}

	public synchronized void dispose() {
		// TODO Finish implementing.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
	 */
	public synchronized void init(ISharedObjectConfig config)
			throws SharedObjectInitException {
		this.config = config;
		Map params = config.getProperties();
		ISharedObjectConfig bootstrapConfig = null;
		ISharedObjectConfig senderConfig = null;
		if (params != null) {
			Object param = params.get("sharedData");
			if (param != null)
				sharedData = param;

			param = params.get("bootstrap");
			if (param != null) {
				SharedObjectDescription sd = (SharedObjectDescription) param;
				try {
					Class bootstrapClass = Class.forName(sd.getClassname());
					bootstrap = (IBootstrap) bootstrapClass.newInstance();
					bootstrapConfig = new ComponentConfig(sd);
				} catch (ClassNotFoundException e) {
					throw new SharedObjectInitException(e);
				} catch (InstantiationException e) {
					throw new SharedObjectInitException(e);
				} catch (IllegalAccessException e) {
					throw new SharedObjectInitException(e);
				}
			}

			param = params.get("sender");
			if (param != null) {
				SharedObjectDescription sd = (SharedObjectDescription) param;
				try {
					Class senderClass = Class.forName(sd.getClassname());
					sender = (AbstractMulticaster) senderClass.newInstance();
					senderConfig = new ComponentConfig(sd);
				} catch (ClassNotFoundException e) {
					throw new SharedObjectInitException(e);
				} catch (InstantiationException e) {
					throw new SharedObjectInitException(e);
				} catch (IllegalAccessException e) {
					throw new SharedObjectInitException(e);
				}
			}

			param = params.get("updateProvider");
			if (param != null)
				updateProvider = UpdateProviderRegistry.createProvider(
						(String) param, null); // TODO what about params?
		}

		bootstrap.setAgent(this);
		bootstrap.init(bootstrapConfig == null ? new ComponentConfig(null)
				: bootstrapConfig);

		sender.addMessageListener(this);
		sender.init(senderConfig == null ? new ComponentConfig(null)
				: senderConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		bootstrap.handleEvent(event);
		sender.handleEvent(event);
		if (event instanceof ISharedObjectActivatedEvent) {
			ISharedObjectActivatedEvent e = (ISharedObjectActivatedEvent) event;
			if (e.getActivatedID().equals(config.getSharedObjectID()))
				handleActivated();
		} else if (event instanceof ISharedObjectMessageEvent) {
			ISharedObjectMessageEvent e = (ISharedObjectMessageEvent) event;
			if (e.getData() instanceof Activated)
				handleActivated(e.getRemoteContainerID(), (Activated) e
						.getData());
		}
	}

	private void handleActivated() {
		if (config.getHomeContainerID().equals(
				config.getContext().getLocalContainerID()))
			try {
				Map params = new HashMap(4);
				params.put("sharedData", sharedData);
				params.put("bootstrap", bootstrap.createDescription());
				params.put("sender", sender.createDescription());
				params.put("updateProvider", updateProvider.getFactory()
						.getID());
				config.getContext().sendCreate(
						null,
						new SharedObjectDescription(config.getSharedObjectID(),
								getClass(), params));
				if (pubCallback != null)
					pubCallback.dataPublished(this);
			} catch (IOException e) {
				handleError(e);
				if (pubCallback != null)
					pubCallback.publicationFailed(this, e);
			} finally {
				pubCallback = null;
			}
	}

	public synchronized void doBootstrap(ID containerID) {
		while (newContainerID != null)
			try {
				wait();
			} catch (InterruptedException e) {
				handleError(e);
			}

		try {
			sender.pause();
		} catch (IllegalStateException e) {
			handleError(e);
			return;
		} catch (ECFException e) {
			handleError(e);
			return;
		}

		Map params = new HashMap(4);
		params.put("sharedData", sharedData);
		params.put("bootstrap", bootstrap.createDescription());
		params.put("sender", sender.createDescription());
		params.put("updateProvider", updateProvider.getFactory().getID());
		try {
			config.getContext().sendCreate(
					containerID,
					new SharedObjectDescription(config.getSharedObjectID(),
							getClass(), params));
			wait(1000);
			newContainerID = null;
		} catch (IOException e) {
			handleError(e);
		} catch (InterruptedException e) {
			handleError(e);
		} finally {
			try {
				sender.resume();
			} catch (IllegalStateException e) {
				handleError(e);
			} catch (ECFException e) {
				handleError(e);
			}
		}
	}

	private synchronized void handleActivated(ID remoteContainerID,
			Activated activated) {
		if (remoteContainerID.equals(newContainerID))
			notify();
	}

	private void handleError(Throwable t) {
		t.printStackTrace();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvents(org.eclipse.ecf.core.util.Event[])
	 */
	public void handleEvents(Event[] events) {
		for (int i = 0; i < events.length; ++i)
			handleEvent(events[i]);
	}

	public synchronized void messageReceived(Version version, Object message) {
		try {
			updateProvider.applyUpdate(this, message);
		} catch (ECFException e) {
			handleError(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
	 */
	public void dispose(ID containerID) {
		sender.dispose(containerID);
		bootstrap.dispose(containerID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class clazz) {
		return null;
	}

	private class ComponentConfig implements ISharedObjectConfig {

		private final SharedObjectDescription sd;

		public ComponentConfig(SharedObjectDescription sd) {
			this.sd = sd;
		}

		public ID getSharedObjectID() {
			return config.getSharedObjectID();
		}

		public ID getHomeContainerID() {
			return config.getHomeContainerID();
		}

		public ISharedObjectContext getContext() {
			return config.getContext();
		}

		public Map getProperties() {
			return sd == null ? null : sd.getProperties();
		}
	}
}
