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
public class Agent implements ISharedData, ISharedObject {

	private Object sharedData;

	private ISharedObjectConfig config;

	private IBootstrap bootstrap;

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
			IUpdateProvider updateProvider, IPublicationCallback pubCallback) {
		this.sharedData = sharedData;
		this.bootstrap = bootstrap;
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
		// lock on transaction (wait till there's none)
		// send prepare
		// collect replies
		// send commit/abort
		Object update = updateProvider.createUpdate(this);
		if (update != null) {
//			Version newVersion = version.getNext(config.getSharedObjectID());
//			Commit msg = new Commit(newVersion, update);
//			version = newVersion;
//			try {
//				config.getContext().sendMessage(null, msg);
//			} catch (IOException e) {
//				throw new ECFException(e);
//			}
		}
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
		if (params != null) {
			Object param = params.get("sharedData");
			if (param != null)
				sharedData = param;

			param = params.get("version");
//			if (param != null)
//				version = (Version) param;

			param = params.get("bootstrap");
			if (param != null)
				bootstrap = ((IBootstrapMemento) param).createBootstrap();

			param = params.get("updateProvider");
			if (param != null)
				updateProvider = UpdateProviderRegistry.createProvider(
						(String) param, null); // TODO what about params?
		}

//		if (version == null)
//			version = new Version(config.getSharedObjectID());

		bootstrap.setAgent(this);
		bootstrap.init(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		bootstrap.handleEvent(event);
		if (event instanceof ISharedObjectActivatedEvent) {
			ISharedObjectActivatedEvent e = (ISharedObjectActivatedEvent) event;
			if (e.getActivatedID().equals(config.getSharedObjectID()))
				handleActivated();
		} else if (event instanceof ISharedObjectMessageEvent) {
			ISharedObjectMessageEvent e = (ISharedObjectMessageEvent) event;
		}
	}

	private void handleActivated() {
		if (config.getHomeContainerID().equals(
				config.getContext().getLocalContainerID()))
			try {
				Map params = new HashMap(3);
				params.put("sharedData", sharedData);
//				params.put("version", version);
				params.put("bootstrap", bootstrap.createMemento());
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

	public void doBootstrap(ID containerID) {
		Map params = new HashMap(3);
		params.put("sharedData", sharedData);
//		params.put("version", version);
		params.put("bootstrap", bootstrap.createMemento());
		params.put("updateProvider", updateProvider.getFactory().getID());
		try {
			config.getContext().sendCreate(
					containerID,
					new SharedObjectDescription(config.getSharedObjectID(),
							getClass(), params));
		} catch (IOException e) {
			handleError(e);
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
	 */
	public synchronized void dispose(ID containerID) {
		bootstrap.dispose(containerID);
		bootstrap = null;
		config = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class clazz) {
		return null;
	}
}
