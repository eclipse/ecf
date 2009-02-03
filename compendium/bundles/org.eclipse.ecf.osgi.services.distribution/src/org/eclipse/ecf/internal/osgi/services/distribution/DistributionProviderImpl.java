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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.distribution.DistributionProvider;

public class DistributionProviderImpl implements DistributionProvider {

	Map exposedServices = Collections.synchronizedMap(new HashMap());
	Map remoteServices = Collections.synchronizedMap(new HashMap());

	Long getServiceId(ServiceReference sr) {
		return (Long) sr.getProperty(Constants.SERVICE_ID);
	}

	ServiceReference addExposedService(ServiceReference sr) {
		if (sr == null)
			return null;
		return (ServiceReference) exposedServices.put(getServiceId(sr), sr);
	}

	ServiceReference addRemoteService(ServiceReference sr) {
		if (sr == null)
			return null;
		return (ServiceReference) remoteServices.put(getServiceId(sr), sr);
	}

	ServiceReference removeExposedService(Long sid) {
		if (sid == null)
			return null;
		return (ServiceReference) exposedServices.remove(sid);
	}

	ServiceReference removeExposedService(ServiceReference sr) {
		return removeExposedService(getServiceId(sr));
	}

	ServiceReference removeRemoteService(Long sid) {
		if (sid == null)
			return null;
		return (ServiceReference) remoteServices.remove(sid);
	}

	ServiceReference removeRemoteService(ServiceReference sr) {
		return removeRemoteService(getServiceId(sr));
	}

	boolean containsExposedService(Long sid) {
		if (sid == null)
			return false;
		return exposedServices.containsKey(sid);
	}

	boolean containsRemoteService(Long sid) {
		if (sid == null)
			return false;
		return remoteServices.containsKey(sid);
	}

	ServiceReference getExposedService(Long sid) {
		if (sid == null)
			return null;
		return (ServiceReference) exposedServices.get(sid);
	}

	ServiceReference getRemoteService(Long sid) {
		if (sid == null)
			return null;
		return (ServiceReference) remoteServices.get(sid);
	}

	public ServiceReference[] getExposedServices() {
		return (ServiceReference[]) exposedServices.entrySet().toArray(
				new ServiceReference[] {});
	}

	public Map getPublicationProperties(ServiceReference sr) {
		// the spec or javadocs don't say what should happen if given sr is null
		// or
		// the given sr is not found in those published...
		Map result = new HashMap();
		if (sr == null)
			return result;
		ServiceReference publishedService = getPublishedService(sr);
		if (publishedService == null)
			return result;
		return getPropertyMap(result, publishedService);
	}

	private ServiceReference getPublishedService(ServiceReference sr) {
		// TODO get from discovery bundle
		return null;
	}

	private Map getPropertyMap(Map result, ServiceReference sr) {
		String[] propKeys = sr.getPropertyKeys();
		if (propKeys != null) {
			for (int i = 0; i < propKeys.length; i++) {
				result.put(propKeys[i], sr.getProperty(propKeys[i]));
			}
		}
		return result;
	}

	public ServiceReference[] getPublishedServices() {
		return org.eclipse.ecf.internal.osgi.services.discovery.Activator
				.getDefault().getServicePublicationHandler()
				.getPublishedServices();
	}

	public ServiceReference[] getRemoteServices() {
		return (ServiceReference[]) remoteServices.entrySet().toArray(
				new ServiceReference[] {});
	}

}
