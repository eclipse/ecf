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
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.events.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.*;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.provider.discovery.*;

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
					// we want to pretend the discovery event comes from us, thus we change the connectedId
					isl.serviceDiscovered(new CompositeServiceContainerEvent(event, getConnectedID()));
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
					// we want to pretend the discovery event comes from us, thus we change the connectedId
					isl.serviceUndiscovered(new CompositeServiceContainerEvent(event, getConnectedID()));
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
					// we want to pretend the discovery event comes from us, thus we change the connectedId
					listener.serviceTypeDiscovered(new CompositeServiceTypeContainerEvent(event, getConnectedID()));
					Trace.trace(Activator.PLUGIN_ID, METHODS_TRACING, this.getClass(), "serviceTypeDiscovered", //$NON-NLS-1$
							"serviceTypeDiscovered fired for listener " //$NON-NLS-1$
									+ listener.toString() + " with event: " + event.toString()); //$NON-NLS-1$
				}
			}
			// add ourself as a listener to the underlying providers. This might
			// trigger a serviceAdded alread
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

	protected static final String METHODS_CATCHING = Activator.PLUGIN_ID + "/debug/methods/catching"; //$NON-NLS-1$

	protected static final String METHODS_TRACING = Activator.PLUGIN_ID + "/debug/methods/tracing"; //$NON-NLS-1$

	protected final CompositeContainerServiceListener ccsl = new CompositeContainerServiceListener();
	protected final CompositeContainerServiceTypeListener ccstl = new CompositeContainerServiceTypeListener();

	/**
	 * History of services registered with this IDCA
	 * 
	 * Used on newly added IDCAs 
	 */
	protected Set registeredServices;

	protected final List containers;

	private ID targetID;

	/**
	 * @param containers
	 */
	public CompositeDiscoveryContainer(List containers) {
		super(CompositeNamespace.NAME, new DiscoveryContainerConfig(IDFactory.getDefault().createStringID(CompositeDiscoveryContainer.class.getName())));
		this.containers = containers;
		this.registeredServices = new HashSet();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#connect(org.eclipse.ecf.core.identity.ID, org.eclipse.ecf.core.security.IConnectContext)
	 */
	public void connect(ID aTargetID, IConnectContext connectContext) throws ContainerConnectException {
		if (targetID != null || getConfig() == null) {
			throw new ContainerConnectException(Messages.CompositeDiscoveryContainer_AlreadyConnected);
		}
		targetID = (aTargetID == null) ? getConfig().getID() : aTargetID;
		fireContainerEvent(new ContainerConnectingEvent(this.getID(), targetID, connectContext));
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
		fireContainerEvent(new ContainerConnectedEvent(this.getID(), targetID));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#disconnect()
	 */
	public void disconnect() {
		fireContainerEvent(new ContainerDisconnectingEvent(this.getID(), getConnectedID()));
		targetID = null;
		synchronized (containers) {
			for (Iterator itr = containers.iterator(); itr.hasNext();) {
				IContainer container = (IContainer) itr.next();
				container.disconnect();
			}
		}
		synchronized (registeredServices) {
			registeredServices.clear();
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
			containers.clear();
		}
		targetID = null;
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IContainer#getConnectedID()
	 */
	public ID getConnectedID() {
		return targetID;
	}

	private IServiceID getServiceIDForDiscoveryContainer(IServiceID service, IDiscoveryContainerAdapter dca) {
		Namespace connectNamespace = dca.getServicesNamespace();
		if (!connectNamespace.equals(service.getNamespace())) {
			return ServiceIDFactory.getDefault().createServiceID(connectNamespace, service.getServiceTypeID().getName(), service.getServiceName());
		}
		return service;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IDiscoveryContainerAdapter#getServiceInfo(org.eclipse.ecf.discovery.identity.IServiceID)
	 */
	public IServiceInfo getServiceInfo(IServiceID aService) {
		Assert.isNotNull(aService);
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
		Assert.isNotNull(type);
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
		Namespace connectNamespace = dca.getServicesNamespace();
		if (!connectNamespace.equals(type.getNamespace())) {
			IServiceID serviceID = (IServiceID) connectNamespace.createInstance(new Object[] {type, null});
			return serviceID.getServiceTypeID();
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
		Assert.isNotNull(serviceInfo);
		synchronized (registeredServices) {
			Assert.isTrue(registeredServices.add(serviceInfo));
		}
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
		Assert.isNotNull(serviceInfo);
		synchronized (registeredServices) {
			// no assert as unregisterService might be called with an non-existing ISI
			registeredServices.remove(serviceInfo);
		}
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
		// register previously registered with the new IDS
		synchronized (registeredServices) {
			IDiscoveryContainerAdapter idca = (IDiscoveryContainerAdapter) object;
			for (Iterator itr = registeredServices.iterator(); itr.hasNext();) {
				IServiceInfo serviceInfo = (IServiceInfo) itr.next();
				try {
					idca.registerService(serviceInfo);
				} catch (ECFException e) {
					// we eat the exception here since the original registerService call is long done
					Trace.catching(Activator.PLUGIN_ID, METHODS_CATCHING, this.getClass(), "addContainer(Object)", e); //$NON-NLS-1$
				}
			}
		}
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

	/**
	 * @return The List of currently registered containers.
	 * @since 2.1
	 */
	public List getDiscoveryContainers() {
		return Collections.unmodifiableList(containers);
	}
}
