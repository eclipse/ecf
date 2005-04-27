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
import org.eclipse.ecf.datashare.IUpdateListener;
import org.eclipse.ecf.datashare.IUpdateProvider;

/**
 * @author pnehrer
 */
public class Agent implements ISharedData, ISharedObject {

	private Object sharedData;

	private ISharedObjectConfig config;

	private IBootstrap bootstrap;

	private IUpdateProvider updateProvider;

	private IUpdateListener updateListener;

	private IPublicationCallback pubCallback;

	public Agent() {
	}

	public Agent(Object sharedData) {
		this.sharedData = sharedData;
	}

	public synchronized ID getID() {
		return config == null ? null : config.getSharedObjectID();
	}

	public Object getData() {
		return sharedData;
	}

	public synchronized void commit() throws ECFException {
	}

	public synchronized void dispose() {
		if (config != null)
			try {
				config.getContext().sendDispose(
						config.getContext().getLocalContainerID());
			} catch (IOException e) {
				handleError(e);
			}
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
		if (params != null)
			sharedData = params.get("sharedData");

		bootstrap = new LazyElectionBootstrap();
		bootstrap.setAgent(this);
		bootstrap.init(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		if (event instanceof ISharedObjectActivatedEvent) {
			ISharedObjectActivatedEvent e = (ISharedObjectActivatedEvent) event;
			if (e.getActivatedID().equals(config.getSharedObjectID()))
				handleActivated();
		} else if (event instanceof ISharedObjectMessageEvent) {
			ISharedObjectMessageEvent e = (ISharedObjectMessageEvent) event;
			if (e.getData() instanceof Update)
				handleUpdate((Update) e.getData());
		}

		bootstrap.handleEvent(event);
	}

	private void handleActivated() {
		if (config.getHomeContainerID().equals(
				config.getContext().getLocalContainerID()))
			try {
				config.getContext().sendCreate(
						null,
						new SharedObjectDescription(config.getSharedObjectID(),
								getClass()));

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
	
	private void handleUpdate(Update upate) {
		// TODO implement me!
	}

	public void doBootstrap(ID containerID) {
		// TODO bootstrap the new member
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
