package org.eclipse.ecf.tests.httpservice.util;
import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/

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
