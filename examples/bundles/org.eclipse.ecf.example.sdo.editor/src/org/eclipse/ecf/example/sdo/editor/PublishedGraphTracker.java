/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.example.sdo.editor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;

/**
 * @author pnehrer
 */
class PublishedGraphTracker extends PlatformObject implements ISharedObject {

	private ISharedObjectConfig config;

	private final Hashtable paths = new Hashtable();

	synchronized void add(String path) throws ECFException {
		if (config == null)
			throw new ECFException("Not connected.");

		try {
			config.getContext().sendMessage(
					null,
					new Object[] { Boolean.TRUE, config.getSharedObjectID(),
							path });
		} catch (IOException e) {
			throw new ECFException(e);
		}

		handleAdded(config.getContext().getLocalContainerID(), path);
	}

	synchronized void remove(String path) throws ECFException {
		if (config == null)
			throw new ECFException("Not connected.");

		try {
			config.getContext().sendMessage(
					null,
					new Object[] { Boolean.FALSE, config.getSharedObjectID(),
							path });
		} catch (IOException e) {
			throw new ECFException(e);
		}

		handleRemoved(config.getContext().getLocalContainerID(), path);
	}

	synchronized boolean isPublished(String path) {
		synchronized (paths) {
			return paths.contains(path);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
	 */
	public synchronized void init(ISharedObjectConfig initData)
			throws SharedObjectInitException {
		if (config == null)
			config = initData;
		else
			throw new SharedObjectInitException("Already initialized.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		if (event instanceof ISharedObjectMessageEvent) {
			Object[] data = (Object[]) ((ISharedObjectMessageEvent) event)
					.getData();
			if (Boolean.TRUE.equals(data[0]))
				handleAdded((ID) data[1], (String) data[2]);
			else
				handleRemoved((ID) data[1], (String) data[2]);
		}
	}

	private void handleAdded(ID containerID, String path) {
		synchronized (paths) {
			HashSet list = (HashSet) paths.get(path);
			if (list == null) {
				list = new HashSet();
				paths.put(path, list);
			}

			list.add(containerID);
		}
	}

	private void handleRemoved(ID containerID, String path) {
		synchronized (paths) {
			HashSet list = (HashSet) paths.get(path);
			if (list != null) {
				list.remove(containerID);
				if (list.isEmpty())
					paths.remove(path);
			}
		}
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
		if (config != null
				&& config.getContext().getLocalContainerID()
						.equals(containerID))
			config = null;
	}
}
