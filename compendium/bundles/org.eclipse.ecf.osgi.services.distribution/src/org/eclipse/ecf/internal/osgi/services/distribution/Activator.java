/*******************************************************************************
 * Copyright (c) 2019 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
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
