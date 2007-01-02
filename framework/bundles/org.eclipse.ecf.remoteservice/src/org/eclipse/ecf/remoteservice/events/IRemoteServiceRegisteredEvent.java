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
package org.eclipse.ecf.remoteservice.events;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;

/**
 * Remote service registered event. Instances of this class will be received
 * when a remote service has been registered.
 * 
 */
public interface IRemoteServiceRegisteredEvent extends IRemoteServiceEvent {

	/**
	 * The ID of the container that registered the service.
	 * 
	 * @return ID of container that registered service. Will not be
	 *         <code>null</code>.
	 */
	public ID getContainerID();

	/**
	 * Get the interface classes associated with/exposed by the remote service.
	 * 
	 * @return String[] set of interface classes exposed by the registered
	 *         remote service. Will not be <code>null</code>, but may be
	 *         empty array.
	 */
	public String[] getClazzes();

	/**
	 * Get the remote service reference for the registered service.
	 * 
	 * @return {@link IRemoteServiceReference} the reference for the registered
	 *         service. Will not be <code>null</code>.
	 */
	public IRemoteServiceReference getReference();
}
