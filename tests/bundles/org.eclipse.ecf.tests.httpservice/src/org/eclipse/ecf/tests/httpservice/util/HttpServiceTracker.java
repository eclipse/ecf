/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.httpservice.util;

import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class HttpServiceTracker extends ServiceTracker {

	public HttpServiceTracker(BundleContext context, ServiceTrackerCustomizer customizer) {
		super(context, HttpService.class.getName(), customizer);
	}

	public HttpServiceTracker(BundleContext context) {
		this(context, null);
	}
	
	public HttpService getHttpService() {
		return (HttpService) getService();
	}
	
	public HttpService[] getHttpServices() {
		return (HttpService[]) getServices();
	}

}
