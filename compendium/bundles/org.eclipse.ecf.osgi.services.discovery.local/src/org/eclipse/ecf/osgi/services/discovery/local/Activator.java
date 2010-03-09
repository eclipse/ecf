/* 
 * Copyright (c) 2009 Siemens Enterprise Communications GmbH & Co. KG, 
 * Germany. All rights reserved.
 *
 * Siemens Enterprise Communications GmbH & Co. KG is a Trademark Licensee 
 * of Siemens AG.
 *
 * This material, including documentation and any related computer programs,
 * is protected by copyright controlled by Siemens Enterprise Communications 
 * GmbH & Co. KG and its licensors. All rights are reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ecf.osgi.services.discovery.local;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.discovery.local";

	// The shared instance
	private static Activator plugin;

	private LogService logService = null;
	private ServiceTracker logServiceTracker;

	private BundleContext context = null;

	private FileBasedDiscoveryImpl discovery = null;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
		plugin = this;
		context = bc;
		logServiceTracker = new ServiceTracker(bc, LogService.class.getName(),
				new ServiceTrackerCustomizer() {

					private LogService logger = null;

					public Object addingService(ServiceReference reference) {
						if (logger == null) {
							LogService logger = (LogService) context
									.getService(reference);
							setLogService(logger);
							return logger;
						}
						return null;
					}

					public void modifiedService(ServiceReference reference,
							Object service) {
					}

					public void removedService(ServiceReference reference,
							Object service) {
						context.ungetService(reference);
						ServiceReference serviceRef = context
								.getServiceReference(LogService.class.getName());
						if (serviceRef == null) {
							setLogService(null);
						} else {
							setLogService((LogService) context
									.getService(serviceRef));
						}
					}

				});
		logServiceTracker.open();
		discovery = new FileBasedDiscoveryImpl(bc, logService);
		discovery.init();

		context.registerService(CommandProvider.class.getName(),
				new DiscoveryCommandProvider(discovery), null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		logServiceTracker.close();
		logServiceTracker = null;
		discovery.destroy();
		discovery = null;
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * 
	 * @param loggerLogService
	 *            instance to set
	 */
	void setLogService(LogService logger) {
		logService = logger;
		FileBasedDiscoveryImpl.setLogService(logService);
	}

	public BundleContext getBundleContext() {
		return context;
	}
}
