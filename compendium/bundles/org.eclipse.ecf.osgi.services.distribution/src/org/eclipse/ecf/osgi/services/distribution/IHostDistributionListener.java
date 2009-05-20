/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.distribution;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceReference;

/**
 * Listener for host distribution events. Services registered with this as their
 * service interface will have their methods called when the distribution
 * implementation events occur.
 * 
 */
public interface IHostDistributionListener {

	/**
	 * Event indicating that a remote service has been registered, with the
	 * given local serviceReference, the given remoteServiceContainer, and the
	 * given remoteRegistration.
	 * 
	 * @param serviceReference
	 *            the ServiceReference of the locally registered service. Will
	 *            not be <code>null</code>.
	 * 
	 * @param remoteServiceContainer
	 *            the remoteServiceContainer that is doing the distribution for
	 *            this remote service. Will not be <code>null</code>.
	 * 
	 * @param remoteRegistration
	 *            The remote service registration created with successful
	 *            registration with the remoteServiceContainer. Will not be
	 *            <code>null</code>.
	 */
	public void registered(ServiceReference serviceReference,
			IRemoteServiceContainer remoteServiceContainer,
			IRemoteServiceRegistration remoteRegistration);

	/**
	 * Event indicating that a remote service has been unregistered.
	 * 
	 * @param serviceReference
	 *            the ServiceReference of the locally registered service. Will
	 *            not be <code>null</code>.
	 * 
	 * @param remoteRegistration
	 *            The remote service registration previously created upon
	 *            registration. Will not be <code>null</code>.
	 */
	public void unregistered(ServiceReference serviceReference,
			IRemoteServiceRegistration remoteRegistration);

}
