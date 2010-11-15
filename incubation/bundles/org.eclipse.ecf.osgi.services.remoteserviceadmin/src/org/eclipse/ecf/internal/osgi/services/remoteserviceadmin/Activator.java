/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.remoteserviceadmin";

	private static BundleContext context;
	private static Activator instance;

	static BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}

	private DiscoveryImpl discovery;
	private TopologyManagerImpl topologyManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.instance = this;
		discovery = new DiscoveryImpl(context);
		topologyManager = new TopologyManagerImpl(context, discovery);
		// start discovery
		discovery.start();
		// start topology manager
		topologyManager.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (discovery != null) {
			discovery.close();
			discovery = null;
		}
		if (topologyManager != null) {
			topologyManager.close();
			topologyManager = null;
		}
		stopSAXParserTracker();
		stopLogServiceTracker();
		Activator.context = null;
		Activator.instance = null;
	}

	public String getFrameworkUUID() {
		if (context == null)
			return null;
		// code get and set the framework uuid property as specified in
		// r2.enterprise.pdf pg 297
		synchronized ("org.osgi.framework.uuid") {
			String result = context.getProperty("org.osgi.framework.uuid");
			if (result == null) {
				UUID newUUID = UUID.randomUUID();
				result = newUUID.toString();
				System.setProperty("org.osgi.framework.uuid",
						newUUID.toString());
			}
			return result;
		}
	}

	// Sax parser

	private Object saxParserFactoryTrackerLock = new Object();
	private ServiceTracker saxParserFactoryTracker;

	public SAXParserFactory getSAXParserFactory() {
		if (instance == null)
			return null;
		synchronized (saxParserFactoryTrackerLock) {
			if (saxParserFactoryTracker == null) {
				saxParserFactoryTracker = new ServiceTracker(context,
						SAXParserFactory.class.getName(), null);
				saxParserFactoryTracker.open();
			}
			return (SAXParserFactory) saxParserFactoryTracker.getService();
		}
	}

	private void stopSAXParserTracker() {
		synchronized (saxParserFactoryTrackerLock) {
			if (saxParserFactoryTracker != null) {
				saxParserFactoryTracker.close();
				saxParserFactoryTracker = null;
			}
		}
	}

	// Logging

	private ServiceTracker logServiceTracker = null;
	private LogService logService = null;
	private Object logServiceTrackerLock = new Object();

	public LogService getLogService() {
		if (context == null)
			return null;
		synchronized (logServiceTrackerLock) {
			if (logServiceTracker == null) {
				logServiceTracker = new ServiceTracker(context,
						LogService.class.getName(), null);
				logServiceTracker.open();
			}
			logService = (LogService) logServiceTracker.getService();
			if (logService == null)
				logService = new SystemLogService(PLUGIN_ID);
			return logService;
		}
	}

	public void log(IStatus status) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(null, LogHelper.getLogCode(status),
					LogHelper.getLogMessage(status), status.getException());
	}

	public void log(ServiceReference sr, IStatus status) {
		log(sr, LogHelper.getLogCode(status), LogHelper.getLogMessage(status),
				status.getException());
	}

	public void log(ServiceReference sr, int level, String message, Throwable t) {
		if (logService == null)
			logService = getLogService();
		if (logService != null)
			logService.log(sr, level, message, t);
	}

	private void stopLogServiceTracker() {
		synchronized (logServiceTrackerLock) {
			if (logServiceTracker != null) {
				logServiceTracker.close();
				logServiceTracker = null;
				logService = null;
			}
		}
	}

	public static List getStringPlusProperty(Map properties, String key) {
		Object value = properties.get(key);
		if (value == null) {
			return Collections.EMPTY_LIST;
		}

		if (value instanceof String) {
			return Collections.singletonList((String) value);
		}

		if (value instanceof String[]) {
			String[] values = (String[]) value;
			List result = new ArrayList(values.length);
			for (int i = 0; i < values.length; i++) {
				if (values[i] != null) {
					result.add(values[i]);
				}
			}
			return Collections.unmodifiableList(result);
		}

		if (value instanceof Collection) {
			Collection values = (Collection) value;
			List result = new ArrayList(values.size());
			for (Iterator iter = values.iterator(); iter.hasNext();) {
				Object v = iter.next();
				if (v instanceof String) {
					result.add((String) v);
				}
			}
			return Collections.unmodifiableList(result);
		}

		return Collections.EMPTY_LIST;
	}

}
