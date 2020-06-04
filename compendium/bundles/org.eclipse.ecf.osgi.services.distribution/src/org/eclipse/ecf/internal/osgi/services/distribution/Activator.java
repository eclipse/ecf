/****************************************************************************
 * Copyright (c) 2019 Composent, Inc. and others.
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
package org.eclipse.ecf.internal.osgi.services.distribution;

import org.eclipse.ecf.core.util.BundleStarter;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.ITopologyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

public class Activator implements BundleActivator {

	private static final String[] DEPENDENTS = new String[] { "org.eclipse.ecf.osgi.services.remoteserviceadmin" }; //$NON-NLS-1$
	private BasicTopologyManager topologyManager;
	private ServiceRegistration topologyManagerRegistration;

	public void start(BundleContext context) throws Exception {
		BundleStarter.startDependents(context, DEPENDENTS, Bundle.RESOLVED | Bundle.STARTING);
		this.topologyManager = new BasicTopologyManager();
		this.topologyManager.start(context);
		this.topologyManagerRegistration = context.registerService(new String[] { EventListenerHook.class.getName(),
				RemoteServiceAdminListener.class.getName(), ITopologyManager.class.getName() }, this.topologyManager,
				null);
	}

	public void stop(BundleContext context) throws Exception {
		if (topologyManagerRegistration != null) {
			topologyManagerRegistration.unregister();
			topologyManagerRegistration = null;
			this.topologyManager.stop();
			this.topologyManager = null;
		}
	}

}
