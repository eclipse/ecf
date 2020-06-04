/****************************************************************************
 * Copyright (c) 2005, 2010 Jan S. Rellermeyer, Systems Group,
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Jan S. Rellermeyer - initial API and implementation
 *    Markus Alexander Kuppe - enhancements and bug fixes
 * 
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package ch.ethz.iks.slp.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * Bundle Activator
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public class Activator implements BundleActivator {

	private static final boolean ENABLE_JSLP = Boolean
			.valueOf(System.getProperty("ch.ethz.iks.slp.enable", "false"));

	/**
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {

		if (ENABLE_JSLP) {
			// create the platform abstraction layer but do not initialize!!!
			SLPCore.platform = new OSGiPlatformAbstraction(context);

			// register the service factories so each consumer gets its own
			// Locator/Activator instance
			context.registerService("ch.ethz.iks.slp.Advertiser", new ServiceFactory() {
				public Object getService(Bundle bundle, ServiceRegistration registration) {
					SLPCore.init();
					SLPCore.initMulticastSocket();
					return new AdvertiserImpl();
				}

				public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
				}
			}, null);
			context.registerService("ch.ethz.iks.slp.Locator", new ServiceFactory() {
				public Object getService(Bundle bundle, ServiceRegistration registration) {
					SLPCore.init();
					return new LocatorImpl();
				}

				public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
				}
			}, null);
		}
	}

	/**
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {

	}
}
