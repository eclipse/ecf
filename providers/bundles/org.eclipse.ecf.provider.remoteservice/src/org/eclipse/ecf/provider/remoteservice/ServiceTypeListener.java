/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.remoteservice;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

/**
 * Helper class for setting up service listeners for a given serviceTypeID.
 */
public class ServiceTypeListener implements IServiceTypeListener {

	private final IDiscoveryLocator discovery;
	private final IServiceTypeID[] serviceTypeIDs;
	final IServiceListener serviceListener;
	private final String[] requiredProperties;

	class ServiceListener implements IServiceListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.discovery.IServiceListener#serviceDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
		 */
		public void serviceDiscovered(IServiceEvent anEvent) {
			final IServiceInfo svcInfo = anEvent.getServiceInfo();
			if (hasRequiredProperties(svcInfo.getServiceProperties()))
				serviceListener.serviceDiscovered(anEvent);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ecf.discovery.IServiceListener#serviceUndiscovered(org.eclipse.ecf.discovery.IServiceEvent)
		 */
		public void serviceUndiscovered(IServiceEvent anEvent) {
			final IServiceInfo svcInfo = anEvent.getServiceInfo();
			if (hasRequiredProperties(svcInfo.getServiceProperties()))
				serviceListener.serviceUndiscovered(anEvent);
		}
	}

	/**
	 * 
	 * @param discovery discovery adapter instance to set up.  Must not be <code>null</code>.
	 * @param serviceListener service listener to receive notifications of service added/removed and resolved notifications.  Must not be <code>null</code>.
	 * @param serviceTypeIDs service type IDs to setup service listeners for.  May be <code>null</code>.  If <code>null</code>, then
	 * all service types will notify the given serviceListener.
	 * @param requiredProperties properties required of the service info
	 * @since 3.0
	 */
	public ServiceTypeListener(IDiscoveryLocator discovery, IServiceListener serviceListener, IServiceTypeID[] serviceTypeIDs, String[] requiredProperties) {
		this.discovery = discovery;
		Assert.isNotNull(this.discovery);
		this.serviceListener = serviceListener;
		Assert.isNotNull(this.serviceListener);
		this.serviceTypeIDs = serviceTypeIDs;
		this.requiredProperties = requiredProperties;
	}

	/**
	 * 
	 * @param discovery discovery adapter instance to set up.  Must not be <code>null</code>.
	 * @param serviceListener service listener to receive notifications of service added/removed and resolved notifications.  Must not be <code>null</code>.
	 * @param serviceTypeID service type IDs to setup service listeners for.  May be <code>null</code>.  If <code>null</code>, then
	 * all service types will notify the given serviceListener.
	 * @param requiredProperties properties required of the service info
	 * @since 3.0
	 */
	public ServiceTypeListener(IDiscoveryLocator discovery, IServiceListener serviceListener, IServiceTypeID serviceTypeID, String[] requiredProperties) {
		this(discovery, serviceListener, new IServiceTypeID[] {serviceTypeID}, requiredProperties);
	}

	/**
	 * 
	 * @param discovery discovery adapter instance to set up.  Must not be <code>null</code>.
	 * @param serviceListener service listener to receive notifications of service added/removed and resolved notifications.  Must not be <code>null</code>.
	 * @param serviceTypeID service type IDs to setup service listeners for.  May be <code>null</code>.  If <code>null</code>, then
	 * all service types will notify the given serviceListener.
	 * @since 3.0
	 */
	public ServiceTypeListener(IDiscoveryLocator discovery, IServiceListener serviceListener, IServiceTypeID serviceTypeID) {
		this(discovery, serviceListener, new IServiceTypeID[] {serviceTypeID}, null);
	}

	/**
	 * @param discovery discovery adapter instance to set up.  Must not be <code>null</code>.
	 * @param serviceListener service listener to receive notifications of service added/removed and resolved notifications.  Must not be <code>null</code>.
	 * @since 3.0
	 */
	public ServiceTypeListener(IDiscoveryLocator discovery, IServiceListener serviceListener) {
		this(discovery, serviceListener, (IServiceTypeID[]) null, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceTypeListener#serviceTypeAdded(org.eclipse.ecf.discovery.IServiceTypeEvent)
	 */
	public final void serviceTypeAdded(IServiceTypeEvent event) {
		final IServiceTypeID remoteServiceTypeID = event.getServiceTypeID();
		if (hasRequiredTypeID(remoteServiceTypeID)) {
			this.discovery.addServiceListener(remoteServiceTypeID, serviceListener);
		}
	}

	private boolean hasRequiredTypeID(IServiceTypeID remoteServiceTypeID) {
		if (serviceTypeIDs == null)
			return true;
		for (int i = 0; i < serviceTypeIDs.length; i++)
			if (remoteServiceTypeID.equals(serviceTypeIDs[i]))
				return true;
		return false;
	}

	boolean hasRequiredProperties(IServiceProperties serviceProperties) {
		if (requiredProperties == null)
			return true;
		for (int i = 0; i < requiredProperties.length; i++)
			if (serviceProperties.getProperty(requiredProperties[i]) == null)
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceTypeListener#serviceTypeDiscovered(org.eclipse.ecf.discovery.IServiceEvent)
	 */
	public void serviceTypeDiscovered(IServiceTypeEvent event) {
		final IServiceTypeID remoteServiceTypeID = event.getServiceTypeID();
		if (hasRequiredTypeID(remoteServiceTypeID)) {
			this.discovery.addServiceListener(remoteServiceTypeID, serviceListener);
		}
	}
}
