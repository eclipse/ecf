/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;

/**
 * Endpoint description advertiser service. TopologyManager consumers may use
 * this service to advertise/publish endpoint descriptions for discovery.
 * Typically, implementations of this service will use the ECF Discovery API to
 * advertise/publish EndpointDescriptions over the network. For example, this is
 * what {@link EndpointDescriptionAdvertiser} does...i.e. it
 * advertises/unadvertises endpoint descriptions by calling and/all available
 * instances of
 * {@link org.eclipse.ecf.discovery.IDiscoveryAdvertiser#registerService(org.eclipse.ecf.discovery.IServiceInfo)}
 * .
 * <p>
 * <p>
 * Note, however, that other implementations of endpoint description advertisers
 * are possible that do not use ECF Discovery...or use ECF Discovery in other
 * ways. For example, some TopologyManagers may wish to advertise exported
 * remote services by creating a static xml file describing the endpoint by
 * using the Endpoint Description Extender Format (EDEF) described in section
 * 122.8 of the <a
 * href="http://www.osgi.org/download/r4v42/r4.enterprise.pdf">OSGi Enterprise
 * Specification</a> by calling {@link #advertise(EndpointDescription)} on their
 * own implementation of this service that uses an
 * {@link EndpointDescriptionWriter} to create an EDEF bundle.
 * <p>
 * <p>
 * If no other instances of this service have been registered, a default
 * instance of {@link EndpointDescriptionAdvertiser} will be used. The default
 * instance uses ECF {@link IDiscoveryAdvertiser}s to publish the endpoint
 * description. Note that the default instance is registered with the lowest
 * possible priority, so that if other {@link IEndpointDescriptionAdvertiser}
 * instances are registered, they will be preferred/used over the default.
 */
public interface IEndpointDescriptionAdvertiser {

	/**
	 * Advertise/publish the given endpoint description.
	 * 
	 * @param endpointDescription
	 *            the endpoint description to advertise. Must not be
	 *            <code>null</code>.
	 * @return IStatus to indicate the status of the advertisement. If the
	 *         returned status returns <code>false</code> from
	 *         {@link IStatus#isOK()}, then the advertisement failed. The
	 *         IStatus can be further inspected for exception information and/or
	 *         child statuses.
	 * 
	 * @see IStatus
	 */
	public IStatus advertise(EndpointDescription endpointDescription);

	/**
	 * Unadvertise/unpublishe the given endpoint description.
	 * 
	 * @param endpointDescription
	 *            the endpoint description to unadvertise. Must not be
	 *            <code>null</code>.
	 * @return IStatus to indicate the status of the unadvertisement. If the
	 *         returned status returns <code>false</code> from
	 *         {@link IStatus#isOK()}, then the unadvertisement failed. The
	 *         IStatus can be further inspected for exception information and/or
	 *         child statuses.
	 * 
	 * @see IStatus
	 */
	public IStatus unadvertise(EndpointDescription endpointDescription);

}
