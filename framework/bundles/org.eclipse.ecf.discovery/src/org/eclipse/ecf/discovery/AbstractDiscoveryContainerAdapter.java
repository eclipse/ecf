/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.AbstractContainer;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

public abstract class AbstractDiscoveryContainerAdapter extends AbstractContainer implements IDiscoveryContainerAdapter, IContainer {

	protected final String servicesNamespaceName;

	protected final DiscoveryContainerConfig config;

	/**
	 * Map of service type to collection of service listeners i.e. <String,Collection<IServiceListener>>.
	 * NOTE: Access to this map is synchronized, so subclasses should take this into account.
	 */
	protected final Map serviceListeners;
	/**
	 * Collection of service type listeners i.e. Collection<IServiceTypeListener>.
	 * NOTE: Access to this collection is synchronized, so subclasses should take this into account.
	 */
	protected final Collection serviceTypeListeners;

	public AbstractDiscoveryContainerAdapter(String aNamespaceName, DiscoveryContainerConfig aConfig) {
		servicesNamespaceName = aNamespaceName;
		Assert.isNotNull(servicesNamespaceName);
		config = aConfig;
		Assert.isNotNull(config);
		serviceTypeListeners = Collections.synchronizedSet(new HashSet());
		serviceListeners = Collections.synchronizedMap(new HashMap());
	}

	protected DiscoveryContainerConfig getConfig() {
		return config;
	}

	/**
	 * @deprecated
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceListener(java.lang.String,
	 *      org.eclipse.ecf.discovery.IServiceListener)
	 */
	public void addServiceListener(String type, IServiceListener listener) {
		if (type == null || listener == null) {
			return;
		}
		synchronized (serviceListeners) { // put-if-absent idiom race condition
			Collection v = (Collection) serviceListeners.get(type);
			if (v == null) {
				v = Collections.synchronizedSet(new HashSet());
				serviceListeners.put(type, v);
			}
			v.add(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceTypeListener(org.eclipse.ecf.discovery.IServiceTypeListener)
	 */
	public void addServiceTypeListener(IServiceTypeListener listener) {
		if (listener == null)
			return;
		synchronized (serviceTypeListeners) {
			serviceTypeListeners.add(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainer#dispose()
	 */
	public void dispose() {
		disconnect();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.IContainer#getConnectNamespace()
	 */
	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(servicesNamespaceName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.IIdentifiable#getID()
	 */
	public ID getID() {
		if (config != null) {
			return config.getID();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceListener(org.eclipse.ecf.discovery.identity.IServiceTypeID, org.eclipse.ecf.discovery.IServiceListener)
	 */
	public void removeServiceListener(IServiceTypeID type, IServiceListener listener) {
		removeServiceListener(type.getName(), listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#addServiceListener(org.eclipse.ecf.discovery.identity.IServiceTypeID, org.eclipse.ecf.discovery.IServiceListener)
	 */
	public void addServiceListener(IServiceTypeID type, IServiceListener listener) {
		addServiceListener(type.getName(), listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceTypeListener(org.eclipse.ecf.discovery.IServiceTypeListener)
	 */
	public void removeServiceTypeListener(IServiceTypeListener listener) {
		if (listener == null)
			return;
		synchronized (serviceTypeListeners) {
			serviceTypeListeners.remove(listener);
		}
	}

	/**
	 * @deprecated
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#removeServiceListener(java.lang.String,
	 *      org.eclipse.ecf.discovery.IServiceListener)
	 */
	public void removeServiceListener(String type, IServiceListener listener) {
		if (type == null || listener == null) {
			return;
		}
		synchronized (serviceListeners) {
			final Collection v = (Collection) serviceListeners.get(type);
			if (v != null) {
				v.remove(listener);
			}
		}
	}

	protected void clearListeners() {
		synchronized (serviceListeners) {
			serviceListeners.clear();
		}
		synchronized (serviceTypeListeners) {
			serviceTypeListeners.clear();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServicesNamespace()
	 */
	public Namespace getServicesNamespace() {
		return IDFactory.getDefault().getNamespaceByName(servicesNamespaceName);
	}

	protected void fireServiceTypeAdded(IServiceEvent serviceEvent) {
		if (serviceEvent == null)
			return;
		List notify = null;
		synchronized (serviceTypeListeners) {
			notify = new ArrayList(serviceTypeListeners);
		}
		for (final Iterator i = notify.iterator(); i.hasNext();) {
			final IServiceTypeListener l = (IServiceTypeListener) i.next();
			l.serviceTypeAdded(serviceEvent);
		}
	}

	protected void fireServiceAdded(IServiceEvent serviceEvent) {
		if (serviceEvent == null)
			return;
		Collection notify = null;
		synchronized (serviceListeners) {
			final Collection orig = (Collection) serviceListeners.get(serviceEvent.getServiceInfo().getServiceID().getServiceTypeID().getName());
			if (orig != null)
				notify = new ArrayList(orig);
		}
		if (notify != null) {
			for (final Iterator i = notify.iterator(); i.hasNext();) {
				final IServiceListener l = (IServiceListener) i.next();
				l.serviceAdded(serviceEvent);
			}
		}
	}

	protected void fireServiceRemoved(IServiceEvent serviceEvent) {
		Collection notify = null;
		synchronized (serviceListeners) {
			final Collection orig = (Collection) serviceListeners.get(serviceEvent.getServiceInfo().getServiceID().getServiceTypeID().getName());
			if (orig != null)
				notify = new ArrayList(orig);
		}
		if (notify != null) {
			for (final Iterator i = notify.iterator(); i.hasNext();) {
				final IServiceListener l = (IServiceListener) i.next();
				l.serviceRemoved(serviceEvent);
			}
		}
	}

	protected void fireServiceResolved(IServiceEvent serviceEvent) {
		if (serviceEvent == null)
			return;
		Collection notify = null;
		synchronized (serviceListeners) {
			final Collection orig = (Collection) serviceListeners.get(serviceEvent.getServiceInfo().getServiceID().getServiceTypeID().getName());
			if (orig != null)
				notify = new ArrayList(orig);
		}
		if (notify != null) {
			for (final Iterator i = notify.iterator(); i.hasNext();) {
				final IServiceListener l = (IServiceListener) i.next();
				l.serviceResolved(serviceEvent);
			}
		}
	}
}
