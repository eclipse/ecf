/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.distribution;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.osgi.framework.ServiceReference;

/**
 * Service interface for customizing the finding of host remote service
 * containers. Services registered with this interfaces will be consulted when a
 * remote service host is registered, in order to select and/or connect
 * IRemoteServiceContainer instances to use to publish remote references.
 */
public interface IHostContainerFinder {

	/**
	 * Find remote service containers. Implementers of this service will be
	 * consulted when a remote service is registered, so that they may provide
	 * access to zero or more {@link IRemoteServiceContainer} instances to use
	 * for distribution and remote service publication and discovery.
	 * 
	 * @param serviceReference
	 *            the ServiceReference of the remote service service
	 *            registration.
	 * @param serviceExportedInterfaces
	 *            The exported interfaces specified by the remote service
	 *            registration. These are the values associated with the
	 *            required service property
	 *            {@link IDistributionConstants#SERVICE_EXPORTED_INTERFACES} as
	 *            per chapter 13 of the OSGi 4.2 compendium specification. Will
	 *            not be <code>null</code>.
	 * @param serviceExportedConfigs
	 *            The exported configuration types specified by the remote
	 *            service registration. These are the values associated with the
	 *            optional service property
	 *            {@link IDistributionConstants#SERVICE_EXPORTED_CONFIGS} as per
	 *            chapter 13 of the OSGi 4.2 compendium specification. May be
	 *            <code>null</code>.
	 * @param serviceIntents
	 *            The service intents specified by the remote service
	 *            registration. These are the values associated with the union
	 *            of the service properties
	 *            {@link IDistributionConstants#SERVICE_INTENTS},
	 *            {@link IDistributionConstants#SERVICE_EXPORTED_INTENTS}, and
	 *            {@link IDistributionConstants#SERVICE_EXPORTED_INTENTS_EXTRA}.
	 *            May be <code>null</code>.
	 * @return IRemoteServiceContainer[] the ECF remote service containers that
	 *         should distribute and publish the remote service (specified by
	 *         the serviceReference) for remote access.
	 */
	public IRemoteServiceContainer[] findHostContainers(
			ServiceReference serviceReference,
			String[] serviceExportedInterfaces,
			String[] serviceExportedConfigs, String[] serviceIntents);

}
