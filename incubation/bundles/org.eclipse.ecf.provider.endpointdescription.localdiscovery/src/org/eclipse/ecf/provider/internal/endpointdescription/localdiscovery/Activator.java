/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.internal.endpointdescription.localdiscovery;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.IServiceInfoFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Activator instance;

	private ServiceTracker parserTracker;

	private ServiceTracker serviceInfoFactoryTracker;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		instance = this;
	}

	public synchronized IServiceInfoFactory getServiceInfoFactory() {
		if (instance == null)
			return null;
		if (serviceInfoFactoryTracker == null) {
			serviceInfoFactoryTracker = new ServiceTracker(context,
					IServiceInfoFactory.class.getName(), null);
			serviceInfoFactoryTracker.open();
		}
		return (IServiceInfoFactory) serviceInfoFactoryTracker.getService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		closeServiceInfoFactoryTracker();
		closeSAXParserTracker();
		Activator.context = null;
		instance = null;
	}

	public static Activator getDefault() {
		return instance;
	}

	public synchronized SAXParserFactory getSAXParserFactory() {
		if (instance == null)
			return null;
		if (parserTracker == null) {
			parserTracker = new ServiceTracker(context,
					SAXParserFactory.class.getName(), null);
			parserTracker.open();
		}
		return (SAXParserFactory) parserTracker.getService();
	}

	private synchronized void closeSAXParserTracker() {
		if (parserTracker != null) {
			parserTracker.close();
			parserTracker = null;
		}
	}
	
	private synchronized void closeServiceInfoFactoryTracker() {
		if (serviceInfoFactoryTracker != null) {
			serviceInfoFactoryTracker.close();
			serviceInfoFactoryTracker = null;
		}
	}
}
