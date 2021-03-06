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
package org.eclipse.ecf.tests.server.generic;

import org.eclipse.ecf.server.generic.IGenericServerContainerGroupFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	private static Activator plugin;
	private BundleContext context;
	
	private ServiceTracker gscgFactoryServiceTracker;
	
	public static Activator getDefault() {
		return plugin;
	}
	
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		if (gscgFactoryServiceTracker != null) {
			gscgFactoryServiceTracker.close();
			gscgFactoryServiceTracker = null;
		}
		this.context = null;
		plugin = null;
	}

	public IGenericServerContainerGroupFactory getGenericServerContainerGroupFactory() {
		if (gscgFactoryServiceTracker == null) {
			gscgFactoryServiceTracker = new ServiceTracker(context,IGenericServerContainerGroupFactory.class.getName(), null);
			gscgFactoryServiceTracker.open();
		}
		return (IGenericServerContainerGroupFactory) gscgFactoryServiceTracker.getService();
	}
}
