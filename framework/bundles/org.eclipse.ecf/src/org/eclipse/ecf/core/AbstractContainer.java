/****************************************************************************
 * Copyright (c) 2006 IBM, Inc and Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Chris Aniszczyk <zx@us.ibm.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core;

import java.util.*;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ecf.core.events.ContainerDisposeEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.security.*;
import org.eclipse.ecf.internal.core.ECFPlugin;

/**
 * Abstract implementation of IContainer. Provides implementations of listener
 * methods that subsclasses may use to avoid having to implement them
 * themselves. This class may be subclassed as needed.
 * 
 */
public abstract class AbstractContainer implements IContainer {

	private final List containerListeners = new ArrayList(5);

	public void addListener(IContainerListener l) {
		synchronized (containerListeners) {
			containerListeners.add(l);
		}
	}

	public void removeListener(IContainerListener l) {
		synchronized (containerListeners) {
			containerListeners.remove(l);
		}
	}

	public void dispose() {
		fireContainerEvent(new ContainerDisposeEvent(getID()));
		synchronized (containerListeners) {
			containerListeners.clear();
		}
	}

	/**
	 * Fires a container event
	 * 
	 * @param event event
	 */
	protected void fireContainerEvent(IContainerEvent event) {
		List toNotify = null;
		// Copy array
		synchronized (containerListeners) {
			toNotify = new ArrayList(containerListeners);
		}
		// Notify all in toNotify
		for (Iterator i = toNotify.iterator(); i.hasNext();) {
			IContainerListener l = (IContainerListener) i.next();
			l.handleEvent(event);
		}
	}

	public <T> T getAdapter(Class<T> serviceType) {
		if (serviceType == null)
			return null;
		if (serviceType.isInstance(this)) {
			return serviceType.cast(this);
		}
		ECFPlugin plugin = ECFPlugin.getDefault();
		if (plugin == null)
			return null;
		IAdapterManager adapterManager = plugin.getAdapterManager();
		return (T) ((adapterManager == null) ? null : adapterManager.loadAdapter(this, serviceType.getName()));
	}

	protected String getPasswordFromConnectContext(IConnectContext connectContext) throws ContainerConnectException {
		String pw = null;
		try {
			Callback[] callbacks = new Callback[1];
			callbacks[0] = new ObjectCallback();
			if (connectContext != null) {
				CallbackHandler handler = connectContext.getCallbackHandler();
				if (handler != null) {
					handler.handle(callbacks);
				}
			}
			ObjectCallback cb = (ObjectCallback) callbacks[0];
			pw = (String) cb.getObject();
		} catch (Exception e) {
			throw new ContainerConnectException("Exception in CallbackHandler.handle(<callbacks>)", e); //$NON-NLS-1$
		}
		return pw;
	}

}
