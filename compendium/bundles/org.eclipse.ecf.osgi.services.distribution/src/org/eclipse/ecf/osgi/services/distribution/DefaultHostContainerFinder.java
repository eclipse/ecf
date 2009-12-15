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

import java.util.Collection;
import java.util.Iterator;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.osgi.framework.ServiceReference;

/**
 * Default implementation of IHostContainerFinder.
 * 
 */
public class DefaultHostContainerFinder extends AbstractContainerFinder
		implements IHostContainerFinder {

	public IRemoteServiceContainer[] findHostContainers(
			ServiceReference serviceReference,
			String[] serviceExportedInterfaces,
			String[] serviceExportedConfigs, String[] serviceIntents) {

		// Find previously created containers for that match the given
		// serviceExportedConfigs and serviceIntents
		Collection rsContainers = findExistingContainers(serviceReference,
				serviceExportedInterfaces, serviceExportedConfigs,
				serviceIntents);

		if (rsContainers.size() == 0) {
			// If no existing containers are found we'll go through
			// finding/creating/configuring/connecting
			rsContainers = createAndConfigureContainers(serviceReference,
					serviceExportedInterfaces, serviceExportedConfigs,
					serviceIntents);

			// if CONTAINER_CONNECT_TARGET service property is specified, then
			// connect container(s)
			Object target = serviceReference
					.getProperty(IDistributionConstants.CONTAINER_CONNECT_TARGET);
			if (target != null) {
				for (Iterator i = rsContainers.iterator(); i.hasNext();) {
					IContainer container = ((IRemoteServiceContainer) i.next())
							.getContainer();
					try {
						doConnectContainer(serviceReference, container, target);
					} catch (Exception e) {
						logException("doConnectContainer failure containerID="
								+ container.getID() + " target=" + target, e);
					}
				}

			}
		}

		// return result
		return (IRemoteServiceContainer[]) rsContainers
				.toArray(new IRemoteServiceContainer[] {});
	}

}
