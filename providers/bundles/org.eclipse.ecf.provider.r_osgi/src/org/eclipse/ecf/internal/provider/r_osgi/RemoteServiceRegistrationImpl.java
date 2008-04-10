/*******************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.internal.provider.r_osgi;

import java.util.Dictionary;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.osgi.framework.ServiceRegistration;

/**
 * The R-OSGi adapter implementation of the IRemoteServiceRegistration.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
final class RemoteServiceRegistrationImpl implements IRemoteServiceRegistration {

	// the container ID.
	private ID containerID;

	// the service registration.
	private ServiceRegistration reg;

	/**
	 * constructor.
	 * 
	 * @param containerID
	 *            the container ID.
	 * @param reg
	 *            the R-OSGi internal service registration.
	 */
	public RemoteServiceRegistrationImpl(final ID containerID, final ServiceRegistration reg) {
		this.containerID = containerID;
		this.reg = reg;
	}

	/**
	 * get the container ID.
	 * 
	 * @return the container ID.
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getContainerID()
	 */
	public ID getContainerID() {
		return containerID;
	}

	/**
	 * get a property of the service.
	 * 
	 * @param key
	 *            the key.
	 * @return the value.
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getProperty(java.lang.String)
	 */
	public Object getProperty(final String key) {
		return reg.getReference().getProperty(key);
	}

	/**
	 * get the property keys.
	 * 
	 * @return the keys.
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getPropertyKeys()
	 */
	public String[] getPropertyKeys() {
		return reg.getReference().getPropertyKeys();
	}

	/**
	 * get the remote service reference; FIXME: problem: with R-OSGi, there is
	 * not necessarily a remote service reference with the registered remote
	 * service.
	 * 
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#getReference()
	 */
	public IRemoteServiceReference getReference() {
		throw new UnsupportedOperationException("The R-OSGi provider does not provide remote service references for local registrations"); //$NON-NLS-1$
	}

	/**
	 * update the properties of the remote service.
	 * 
	 * @param properties
	 *            a set of property key/value pairs to be updated.
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#setProperties(java.util.Dictionary)
	 */
	public void setProperties(final Dictionary properties) {
		reg.setProperties(properties);
	}

	/**
	 * unregister the remote service.
	 * 
	 * @see org.eclipse.ecf.remoteservice.IRemoteServiceRegistration#unregister()
	 */
	public void unregister() {
		reg.unregister();
	}
}
