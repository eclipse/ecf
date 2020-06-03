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
import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;


public class ExtendedHttpServiceTracker extends ServiceTracker {

	public ExtendedHttpServiceTracker(BundleContext context, ServiceTrackerCustomizer customizer) {
		super(context, ExtendedHttpService.class.getName(), customizer);
	}
	
	public ExtendedHttpServiceTracker(BundleContext context) {
		this(context, null);
	}

	public ExtendedHttpService getExtendedHttpService() {
		return (ExtendedHttpService) getService();
	}
	
	public ExtendedHttpService[] getExtendedHttpServices() {
		return (ExtendedHttpService[]) getServices();
	}
	
}
