/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.internal.endpointdescription.localdiscovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.discovery.AbstractDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.DiscoveryContainerConfig;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceTypeEvent;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

public class EndpointDescriptionDiscoveryContainerAdapter extends
		AbstractDiscoveryContainerAdapter {

	private Map<IServiceID, IServiceInfo> remoteServiceInfos;

	public EndpointDescriptionDiscoveryContainerAdapter(String aNamespaceName,
			DiscoveryContainerConfig aConfig) {
		super(aNamespaceName, aConfig);
		remoteServiceInfos = new HashMap<IServiceID, IServiceInfo>();
	}

	public IServiceInfo getServiceInfo(IServiceID aServiceID) {
		if (aServiceID == null)
			return null;
		synchronized (remoteServiceInfos) {
			return remoteServiceInfos.get(aServiceID);
		}
	}

	public IServiceInfo[] getServices() {
		Collection<IServiceInfo> results = new ArrayList<IServiceInfo>();
		synchronized (remoteServiceInfos) {
			for (IServiceInfo i : remoteServiceInfos.values())
				results.add(i);
		}
		return (IServiceInfo[]) results.toArray(new IServiceInfo[] {});
	}

	public IServiceInfo[] getServices(IServiceTypeID aServiceTypeID) {
		Collection<IServiceInfo> results = new ArrayList<IServiceInfo>();
		if (aServiceTypeID == null)
			return (IServiceInfo[]) results.toArray(new IServiceInfo[] {});
		synchronized (remoteServiceInfos) {
			for (IServiceInfo i : remoteServiceInfos.values()) {
				if (i.getServiceID().getServiceTypeID().equals(aServiceTypeID))
					results.add(i);
			}
		}
		return (IServiceInfo[]) results.toArray(new IServiceInfo[] {});
	}

	private Collection<IServiceTypeID> getServiceTypesAsCollection() {
		Collection<IServiceTypeID> results = new ArrayList<IServiceTypeID>();
		synchronized (remoteServiceInfos) {
			for (IServiceInfo i : remoteServiceInfos.values())
				results.add(i.getServiceID().getServiceTypeID());
		}
		return results;
	}

	public IServiceTypeID[] getServiceTypes() {
		return (IServiceTypeID[]) getServiceTypesAsCollection().toArray(
				new IServiceTypeID[] {});
	}

	public void registerService(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			throw new NullPointerException("serviceInfo cannot be null"); //$NON-NLS-1$
		addServiceInfo(serviceInfo);
	}

	private void addServiceInfo(IServiceInfo serviceInfo) {
		IServiceID serviceID = serviceInfo.getServiceID();
		IServiceInfo addResult = null;
		IServiceTypeID serviceTypeAdded = null;
		synchronized (remoteServiceInfos) {
			// First add..
			addResult = remoteServiceInfos.put(serviceID, serviceInfo);
			if (addResult == null) {
				Collection<IServiceTypeID> serviceTypes = getServiceTypesAsCollection();
				IServiceTypeID stID = serviceID.getServiceTypeID();
				if (serviceTypes.contains(stID))
					serviceTypeAdded = stID;
			}
		}
		if (serviceTypeAdded != null)
			fireServiceTypeDiscovered(createDiscoveredServiceTypeEvent(serviceTypeAdded));
		if (addResult == null)
			fireServiceDiscovered(createDiscoveredServiceEvent(serviceInfo));
	}

	private IServiceTypeEvent createDiscoveredServiceTypeEvent(
			final IServiceTypeID serviceTypeID) {
		return new IServiceTypeEvent() {

			public ID getLocalContainerID() {
				return getID();
			}

			public IServiceTypeID getServiceTypeID() {
				return serviceTypeID;
			}
		};
	}

	private IServiceEvent createDiscoveredServiceEvent(
			final IServiceInfo serviceInfo) {
		return new IServiceEvent() {

			public ID getLocalContainerID() {
				return getID();
			}

			public IServiceInfo getServiceInfo() {
				return serviceInfo;
			}
		};
	}

	public void unregisterService(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			throw new NullPointerException("serviceInfo cannot be null"); //$NON-NLS-1$
		removeServiceInfo(serviceInfo);
	}

	private void removeServiceInfo(IServiceInfo serviceInfo) {
		IServiceID serviceID = serviceInfo.getServiceID();
		IServiceInfo addResult = null;
		synchronized (remoteServiceInfos) {
			addResult = remoteServiceInfos.remove(serviceID);
		}
		if (addResult == null)
			fireServiceUndiscovered(createUndiscoveredServiceEvent(serviceInfo));
	}

	private IServiceEvent createUndiscoveredServiceEvent(
			final IServiceInfo serviceInfo) {
		return new IServiceEvent() {

			public ID getLocalContainerID() {
				return getID();
			}

			public IServiceInfo getServiceInfo() {
				return serviceInfo;
			}
		};
	}

	public void connect(ID targetID, IConnectContext connectContext)
			throws ContainerConnectException {
		// Do nothing...no connection
	}

	public ID getConnectedID() {
		return getID();
	}

	public void disconnect() {
		// No disconnection
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		// No adapters supported
		return null;
	}

	@Override
	public String getContainerName() {
		return "ecf.discovery.local"; //$NON-NLS-1$
	}

	public void dispose() {
		remoteServiceInfos.clear();
		super.dispose();
	}
}
