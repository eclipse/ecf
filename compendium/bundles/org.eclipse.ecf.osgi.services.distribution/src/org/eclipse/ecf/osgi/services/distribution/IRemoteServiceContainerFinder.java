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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.osgi.services.discovery.IServiceEndpointDescription;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;

public interface IRemoteServiceContainerFinder {

	/**
	 * 
	 * Find remote service containers.
	 * 
	 * @param endpointDescription
	 *            the endpoint description created from the discovered remote
	 *            service meta data. This endpointDescription may be used to
	 *            decide what IRemoteServiceContainer[] to return, as well as
	 *            whether or not to connect the IContainer to the targetID
	 *            (provided by
	 *            {@link IServiceEndpointDescription#getECFTargetID()}. Will not
	 *            be <code>null</code>.
	 * 
	 * @param monitor
	 *            a progress monitor to report progress or cancel operation from
	 *            within the find. Will not be <code>null</code>.
	 * @return IRemoteServiceContainer[] the remote service containers that
	 *         should be used to get remote service references for the remote
	 *         service described by the endpointDescription. If no containers
	 *         are relevant, then an empty array should be returned rather than
	 *         <code>null</code>.
	 */
	public IRemoteServiceContainer[] findRemoteServiceContainers(
			IServiceEndpointDescription endpointDescription,
			IProgressMonitor monitor);

}
