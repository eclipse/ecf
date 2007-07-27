/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.core;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ecf.core.events.ContainerDisposeEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.core.ECFPlugin;

/**
 * Base implementation of IContainer.  Subclasses may be created to fill out the behavior
 * of this base implementation.  Also, adapter factories may be created via adapterFactory
 * extension point to allow adapters to be added to this BaseContainer implementation
 * without the need to create a separate IContainer implementation class.
 */
public class BaseContainer implements IContainer {

	private Vector listeners = null;

	public static class Instantiator extends BaseContainerInstantiator {

		public IContainer createInstance(ContainerTypeDescription description,
				Object[] parameters) throws ContainerCreateException {
			return new BaseContainer();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
		 */
		public String[] getSupportedAdapterTypes(
				ContainerTypeDescription description) {
			return getInterfacesAndAdaptersForClass(BaseContainer.class);
		}
	}
	
	public BaseContainer() {
		listeners = new Vector();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#addListener(org.eclipse.ecf.core.IContainerListener)
	 */
	public void addListener(IContainerListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void connect(ID targetID, IConnectContext connectContext)
			throws ContainerConnectException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#dispose()
	 */
	public void dispose() {
		fireContainerEvent(new ContainerDisposeEvent(getID()));
		if (listeners != null) {
			listeners.clear();
			listeners = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class serviceType) {
		if (serviceType == null) return null;
		if (serviceType.isInstance(this)) {
			return this;
		} else {
			IAdapterManager adapterManager = ECFPlugin.getDefault().getAdapterManager();
			if (adapterManager == null) return null;
			return adapterManager.loadAdapter(this, serviceType.getName());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectNamespace()
	 */
	public Namespace getConnectNamespace() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#removeListener(org.eclipse.ecf.core.IContainerListener)
	 */
	public void removeListener(IContainerListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		return null;
	}

	protected void fireContainerEvent(IContainerEvent event) {
		synchronized (listeners) {
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				IContainerListener l = (IContainerListener) i.next();
				l.handleEvent(event);
			}
		}
	}

}
