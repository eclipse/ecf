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
package org.eclipse.ecf.provider.discovery;

import java.util.*;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.events.ContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectingEvent;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.provider.discovery.Activator;
import org.eclipse.ecf.internal.provider.discovery.CompositeNamespace;

public class CompositeDiscoveryContainer extends AbstractDiscoveryContainerAdapter implements IDiscoveryService {

	public static final String NAME = "ecf.discovery.composite"; //$NON-NLS-1$

	protected class CompositeContainerServiceListener implements IServiceListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.discovery.IServiceListener#serviceDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
		 */
		public void serviceDiscovered(IServiceEvent event) {
			Collection col = getListeners(event.getServiceInfo().getServiceID().getServiceTypeID());
			if (!col.isEmpty()) {
				for (Iterator itr = col.iterator(); itr.hasNext();) {
					IServiceListener isl = (IServiceListener) itr.next();
					isl.serviceDiscovered(event);
					Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceDiscovered", //$NON-NLS-1$
							"serviceResolved fired for listener " //$NON-NLS-1$
									+ isl.toString() + " with event: " + event.toString()); //$NON-NLS-1$
				}
			} else {
				Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceDiscovered", //$NON-NLS-1$
						"serviceResolved fired without any listeners present"); //$NON-NLS-1$
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.discovery.IServiceListener#serviceUndiscovered(org.eclipse.ecf.discovery.IServiceEvent)
		 */
		public void serviceUndiscovered(IServiceEvent event) {
			Collection col = getListeners(event.getServiceInfo().getServiceID().getServiceTypeID());
			if (!col.isEmpty()) {
				for (Iterator itr = col.iterator(); itr.hasNext();) {
					IServiceListener isl = (IServiceListener) itr.next();
					isl.serviceUndiscovered(event);
					Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceUndiscovered", //$NON-NLS-1$
							"serviceRemoved fired for listener " //$NON-NLS-1$
									+ isl.toString() + " with event: " + event.toString()); //$NON-NLS-1$
				}
			} else {
				Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceUndiscovered", //$NON-NLS-1$
						"serviceRemoved fired without any listeners present"); //$NON-NLS-1$
			}
		}
	}

	protected class CompositeContainerServiceTypeListener implements IServiceTypeListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.discovery.IServiceTypeListener#serviceTypeDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
		 */
		public synchronized void serviceTypeDiscovered(IServiceTypeEvent event) {
			// notify our listeners first so they get a chance to register for
			// the type before the underlying provider fires service added
			synchronized (serviceTypeListeners) {
				for (Iterator itr = serviceTypeListeners.iterator(); itr.hasNext();) {
					IServiceTypeListener listener = (IServiceTypeListener) itr.next();
					listener.serviceTypeDiscovered(event);
					Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceTypeDiscovered", //$NON-NLS-1$
							"serviceTypeDiscovered fired for listener " //$NON-NLS-1$
									+ listener.toString() + " with event: " + event.toString()); //$NON-NLS-1$
				}
			}
			// add ourself as a listener to the underlying providers. This might
			// trigger a serviceAdded already
			IServiceTypeID istid = event.getServiceTypeID();
			synchronized (containers) {
				for (Iterator itr = containers.iterator(); itr.hasNext();) {
					// TODO ccstl doesn't have to be a listener for a non
					// matching (namespace) container, but it doesn't hurt
					// either
					IDiscoveryContainerAdapter idca = (IDiscoveryContainerAdapter) itr.next();
					idca.addServiceListener(istid, ccsl);
				}
			}
		}
	}

	private static final String METHODS_CATCHING = Activator.PLUGIN_ID + "/debug/methods/catching"; //$NON-NLS-1$

	private static final String METHODS_TRACING = Activator.PLUGIN_ID + "/debug/methods/tracing"; //$NON-NLS-1$

	protected CompositeContainerServiceListener ccsl = new CompositeContainerServiceListener();
	protected CompositeContainerServiceTypeListener ccstl = new CompositeContainerServiceTypeListener();

	protected List containers = new ArrayList();

	/**
	 * @param containers
	 * @throws IDCreateException
	 */
	public CompositeDiscoveryContainer(List containers) throws IDCreateException {
		super(CompositeNamespace.NAME, new DiscoveryContainerConfig(IDFactory.getDefault().createStringID(CompositeDiscoveryContainer.class.getName())));
		this.containers = containers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void connect(ID targetID, IConnectContext connectContext) throws ContainerConnectException {
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IContainer container = (IContainer) itr.next();
				if (container.getConnectedID() == null) {
					container.connect(targetID, connectContext);
				}
				IDiscoveryContainerAdapter idca = (IDiscoveryContainerAdapter) container;
				idca.addServiceListener(ccsl);
				idca.addServiceTypeListener(ccstl);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		fireContainerEvent(new ContainerDisconnectingEvent(this.getID(), getConnectedID()));
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IContainer container = (IContainer) itr.next();
				container.disconnect();
			}
		}
		fireContainerEvent(new ContainerDisconnectedEvent(this.getID(), getConnectedID()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#dispose()
	 */
	public void dispose() {
		disconnect();
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IContainer container = (IContainer) itr.next();
				container.dispose();
			}
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return getID();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter#getConnectNamespace()
	 */
	public Namespace getConnectNamespace() {
		//TODO-mkuppe implement CompositeDiscoveryContainer#getConnectNamespace
		throw new java.lang.UnsupportedOperationException("CompositeDiscoveryContainer#getConnectNamespace not yet implemented"); //$NON-NLS-1$
	}

	private IServiceID getServiceIDForDiscoveryContainer(IServiceID service, IDiscoveryContainerAdapter dca) {
		Namespace connectNamespace = dca.getConnectNamespace();
		if (!connectNamespace.equals(service.getNamespace())) {
			try {
				return ServiceIDFactory.getDefault().createServiceID(connectNamespace, service.getServiceTypeID().getName(), service.getName());
			} catch (IDCreateException e) {
				Trace.catching(Activator.PLUGIN_ID, METHODS_CATCHING, this.getClass(), "getServiceTypeIDForDiscoveryContainer", e); //$NON-NLS-1$
			}
		}
		return service;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID)
	 */
	public IServiceInfo getServiceInfo(IServiceID aService) {
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IDiscoveryContainerAdapter idca = (IDiscoveryContainerAdapter) itr.next();
				IServiceID isi = getServiceIDForDiscoveryContainer(aService, idca);
				IServiceInfo service = idca.getServiceInfo(isi);
				if (service != null) {
					return service;
				}
			}
		}
		return null;
	}

	private IServiceInfo getServiceInfoForDiscoveryContainer(IServiceInfo aSi, IDiscoveryContainerAdapter idca) {
		IServiceID serviceID = getServiceIDForDiscoveryContainer(aSi.getServiceID(), idca);
		return new ServiceInfo(aSi.getLocation(), serviceID, aSi.getPriority(), aSi.getWeight(), aSi.getServiceProperties());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices()
	 */
	public IServiceInfo[] getServices() {
		Set set = new HashSet();
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IDiscoveryContainerAdapter idca = (IDiscoveryContainerAdapter) itr.next();
				IServiceInfo[] services = idca.getServices();
				set.addAll(Arrays.asList(services));
			}
		}
		return (IServiceInfo[]) set.toArray(new IServiceInfo[set.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServices(org.eclipse.ecf.discovery.identity.IServiceTypeID)
	 */
	public IServiceInfo[] getServices(IServiceTypeID type) {
		Set set = new HashSet();
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IDiscoveryContainerAdapter idca = (IDiscoveryContainerAdapter) itr.next();
				IServiceTypeID isti = getServiceTypeIDForDiscoveryContainer(type, idca);
				IServiceInfo[] services = idca.getServices(isti);
				set.addAll(Arrays.asList(services));
			}
		}
		return (IServiceInfo[]) set.toArray(new IServiceInfo[set.size()]);
	}

	private IServiceTypeID getServiceTypeIDForDiscoveryContainer(IServiceTypeID type, IDiscoveryContainerAdapter dca) {
		Namespace connectNamespace = dca.getConnectNamespace();
		if (!connectNamespace.equals(type.getNamespace())) {
			try {
				return (IServiceTypeID) connectNamespace.createInstance(new Object[] {type});
			} catch (IDCreateException e) {
				Trace.catching(Activator.PLUGIN_ID, METHODS_CATCHING, this.getClass(), "getServiceTypeIDForDiscoveryContainer", e); //$NON-NLS-1$
			}
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceTypes()
	 */
	public IServiceTypeID[] getServiceTypes() {
		Set set = new HashSet();
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IDiscoveryContainerAdapter idca = (IDiscoveryContainerAdapter) itr.next();
				IServiceTypeID[] services = idca.getServiceTypes();
				set.addAll(Arrays.asList(services));
			}
		}
		return (IServiceTypeID[]) set.toArray(new IServiceTypeID[set.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#registerService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void registerService(IServiceInfo serviceInfo) throws ECFException {
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IDiscoveryContainerAdapter dca = (IDiscoveryContainerAdapter) itr.next();
				IServiceInfo isi = getServiceInfoForDiscoveryContainer(serviceInfo, dca);
				dca.registerService(isi);
				Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "registerService", "registeredService " //$NON-NLS-1$ //$NON-NLS-2$
						+ serviceInfo.toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#unregisterService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public void unregisterService(IServiceInfo serviceInfo) throws ECFException {
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IDiscoveryContainerAdapter idca = (IDiscoveryContainerAdapter) itr.next();
				IServiceInfo isi = getServiceInfoForDiscoveryContainer(serviceInfo, idca);
				idca.unregisterService(isi);
			}
		}
	}

	/**
	 * @param object
	 * @return true on success
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean addContainer(Object object) {
		synchronized (containers) {
			Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "addContainer(Object)", "addContainer " //$NON-NLS-1$ //$NON-NLS-2$
					+ object.toString());
			return containers.add(object);
		}
	}

	/**
	 * @param object
	 * @return true on success
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean removeContainer(Object object) {
		synchronized (containers) {
			Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "removeContainer(Object)", "removeContainer " //$NON-NLS-1$ //$NON-NLS-2$
					+ object.toString());
			return containers.remove(object);
		}
	}
}
