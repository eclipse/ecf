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

import java.util.Hashtable;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.ISharedObjectContainerListener;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.events.ISharedObjectContainerDisposeEvent;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IDataShareService;
import org.eclipse.ecf.datashare.IDataShareServiceManager;

/**
 * @author pnehrer
 */
public class ServiceManager implements IDataShareServiceManager {

	private final Hashtable instances = new Hashtable();

	private final Hashtable listeners = new Hashtable();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IDataShareServiceManager#getInstance(org.eclipse.ecf.core.ISharedObjectContainer)
	 */
	public synchronized IDataShareService getInstance(
			ISharedObjectContainer container) throws ECFException {
		IDataShareService svc = (IDataShareService) instances.get(container);
		if (svc == null) {
			svc = new DataShareService(this, container);
			instances.put(container, svc);
			DisposeListener listener = new DisposeListener(container);
			listeners.put(container, listener);
			container.addListener(listener, null);
		}

		return svc;
	}

	public synchronized void dispose(ISharedObjectContainer container) {
		instances.remove(container);
		listeners.remove(container);
	}

	private class DisposeListener implements ISharedObjectContainerListener {

		private final ISharedObjectContainer container;

		public DisposeListener(ISharedObjectContainer container) {
			this.container = container;
		}

		public void handleEvent(IContainerEvent evt) {
			if (evt instanceof ISharedObjectContainerDisposeEvent)
				dispose(container);
		}
	}
}
