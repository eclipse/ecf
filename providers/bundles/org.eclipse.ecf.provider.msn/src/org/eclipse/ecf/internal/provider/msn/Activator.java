/****************************************************************************
 * Copyright (c) 2006, 2007 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.msn;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.presence.service.IPresenceService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public final class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.msn"; //$NON-NLS-1$

	static final String NAMESPACE_ID = "ecf.msn.msnp"; //$NON-NLS-1$

	private static Activator plugin;

	private BundleContext context;

	private Map services;

	public Activator() {
		plugin = this;
	}

	void registerService(IPresenceService service) {
		services.put(service, context.registerService(IPresenceService.class
				.getName(), service, null));
	}

	void unregisterService(IPresenceService service) {
		ServiceRegistration registration = (ServiceRegistration) services
				.remove(service);
		if (registration != null) {
			registration.unregister();
		}
	}

	public void start(BundleContext context) throws Exception {
		this.context = context;
		services = new HashMap();
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	public synchronized static Activator getDefault() {
		if (plugin == null) {
			plugin = new Activator();
		}
		return plugin;
	}

}
