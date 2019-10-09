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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.ITopologyManager;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.TopologyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

public class BasicTopologyManager extends TopologyManager
		implements EventListenerHook, RemoteServiceAdminListener, ITopologyManager {

	public void start(BundleContext context) throws Exception {
		activate(context, new HashMap<String, Object>());
	}

	@Override
	protected void activate(BundleContext context, Map<String, ?> properties) throws Exception {
		super.activate(context, properties);
	}

	public void stop() {
		deactivate();
	}

	@Override
	protected void deactivate() {
		super.deactivate();
	}
}
