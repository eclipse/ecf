/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.*;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.distribution.DistributionProvider;

public class DistributionProviderImpl implements DistributionProvider {

	public static Object VENDOR_NAME = "Eclipse Foundation";
	public static Object PRODUCT_NAME = "Eclipse Communication Framework (ECF)";
	public static Object PRODUCT_VERSION = "3.0";

	List exposedServices = Collections.synchronizedList(new ArrayList());
	List remoteServices = Collections.synchronizedList(new ArrayList());

	boolean addExposedService(ServiceReference sr) {
		if (sr == null)
			return false;
		return exposedServices.add(sr);
	}

	boolean addRemoteService(ServiceReference sr) {
		if (sr == null)
			return false;
		return remoteServices.add(sr);
	}

	boolean removeExposedService(ServiceReference sr) {
		if (sr == null)
			return false;
		return exposedServices.remove(sr);
	}

	boolean removeRemoteService(ServiceReference sr) {
		if (sr == null)
			return false;
		return remoteServices.remove(sr);
	}

	public Collection getExposedServices() {
		return exposedServices;
	}

	public Map getExposedProperties(ServiceReference sr) {
		Map result = new HashMap();
		if (sr == null)
			return result;
		return result;
	}

	public void dispose() {
		exposedServices.clear();
		remoteServices.clear();
	}

	public Collection getRemoteServices() {
		return remoteServices;
	}

	public Collection getSupportedIntents() {
		List result = new ArrayList();
		IContainerFactory containerFactory = Activator.getDefault()
				.getContainerManager().getContainerFactory();
		List containerDescriptions = containerFactory.getDescriptions();
		if (containerDescriptions != null) {
			for (Iterator i = containerDescriptions.iterator(); i.hasNext();) {
				ContainerTypeDescription ctd = (ContainerTypeDescription) i
						.next();
				String[] adapterTypes = ctd.getSupportedAdapterTypes();
				if (adapterTypes != null) {
					List at = Arrays.asList(adapterTypes);
					if (at.contains(IRemoteServiceContainerAdapter.class
							.getName())) {
						String[] intents = ctd.getSupportedIntents();
						if (intents != null) {
							for (int j = 0; j < intents.length; j++) {
								if (!result.contains(intents[j]))
									result.add(intents[j]);
							}
						}
					}
				}
			}
		}
		return result;
	}

}
