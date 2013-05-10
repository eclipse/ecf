/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Dictionary;
import java.util.Properties;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.service.EventHook;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	private static final boolean allowLoopbackReference = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.discovery.allowLoopbackReference", //$NON-NLS-1$
					"false")).booleanValue(); //$NON-NLS-1$

	private static final String endpointListenerScope = System
			.getProperty("org.eclipse.ecf.osgi.services.discovery.endpointListenerScope"); //$NON-NLS-1$

	public static final String PLUGIN_ID = "org.eclipse.ecf.osgi.services.distribution"; //$NON-NLS-1$

	public static final boolean autoCreateProxyContainer = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.distribution.autoCreateProxyContainer", //$NON-NLS-1$
					"true")).booleanValue(); //$NON-NLS-1$

	public static final boolean autoCreateHostContainer = new Boolean(
			System.getProperty(
					"org.eclipse.ecf.osgi.services.distribution.autoCreateHostContainer", //$NON-NLS-1$
					"true")).booleanValue(); //$NON-NLS-1$

	public static final String defaultHostConfigType = System.getProperty(
			"org.eclipse.ecf.osgi.services.distribution.defaultConfigType", //$NON-NLS-1$
			"ecf.generic.server"); //$NON-NLS-1$

	private static final String PROP_USE_DS = "equinox.use.ds"; //$NON-NLS-1$

	private static Activator plugin;
	private BundleContext context;

	private ServiceTracker logServiceTracker = null;
	private LogService logService = null;

	private BasicTopologyManagerImpl basicTopologyManagerImpl;
	private ServiceRegistration endpointListenerReg;
	private BasicTopologyManagerComponent basicTopologyManagerComp;
	private ServiceRegistration eventHookRegistration;

	public static Activator getDefault() {
		return plugin;
	}

	public BundleContext getContext() {
		return context;
	}

	protected LogService getLogService() {
		if (this.context == null)
			return null;
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context,
					LogService.class.getName(), null);
			logServiceTracker.open();
		}
		logService = (LogService) logServiceTracker.getService();
		if (logService == null)
			logService = new SystemLogService(PLUGIN_ID);
		return logService;
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

	private String getEndpointListenerScope() {
		// If it's set via system property, then simply use it
		if (endpointListenerScope != null)
			return endpointListenerScope;
		// Otherwise create it
		// if allowLoopbackReference is true, then return a filter to match all
		// endpoint description ids
		StringBuffer elScope = new StringBuffer("("); //$NON-NLS-1$
		if (allowLoopbackReference) {
			elScope.append(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID);
			elScope.append("=*"); //$NON-NLS-1$
		} else {
			// filter so that local framework uuid is not the same as local
			// value
			elScope.append("!("); //$NON-NLS-1$
			elScope.append(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID);
			elScope.append("="); //$NON-NLS-1$
			elScope.append(basicTopologyManagerImpl.getFrameworkUUID());
			elScope.append(")"); //$NON-NLS-1$
		}
		elScope.append(")"); //$NON-NLS-1$
		return elScope.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(final BundleContext ctxt) throws Exception {
		// Always set plugin and context
		plugin = this;
		this.context = ctxt;
		// Create basicTopologyManagerImpl
		basicTopologyManagerImpl = new BasicTopologyManagerImpl(context);

		// Register basicTopologyManagerImpl as EndpointListener always, so that
		// it
		// gets notified when Endpoints are discovered
		Properties props = new Properties();
		props.put(
				org.osgi.service.remoteserviceadmin.EndpointListener.ENDPOINT_LISTENER_SCOPE,
				getEndpointListenerScope());
		endpointListenerReg = getContext().registerService(
				EndpointListener.class.getName(), basicTopologyManagerImpl,
				(Dictionary) props);

		// Like EventAdmin, if equinox ds is running, then we simply return (no
		// more to do)
		if (Boolean.valueOf(context.getProperty(PROP_USE_DS)).booleanValue())
			return; // If this property is set we assume DS is being used.

		// The following code is to make sure that we don't do any more if
		// EventHook has already been registered for us by DS
		// Create serviceFilter for EventHook classname
		String serviceName = EventHook.class.getName();
		Filter serviceFilter = context
				.createFilter("(objectclass=" + serviceName + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		// if this bundle has already registered EventHook service via ds, then
		// we're done
		ServiceReference[] refs = context.getBundle().getRegisteredServices();
		if (refs != null) {
			for (int i = 0; i < refs.length; i++)
				if (serviceFilter.match(refs[i]))
					return; // We found a service registered by this bundle
							// already so we return
		}

		// Otherwise (no DS), we create a basicTopologyManagerComponent
		basicTopologyManagerComp = new BasicTopologyManagerComponent();
		// bind the topology manager to it
		basicTopologyManagerComp.bindEndpointListener(basicTopologyManagerImpl);
		// register the basic topology manager as EventHook service
		eventHookRegistration = this.context.registerService(EventHook.class,
				basicTopologyManagerComp, null);
		// export any previously registered remote services by calling activate
		basicTopologyManagerComp.activate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext ctxt) throws Exception {
		if (eventHookRegistration != null) {
			eventHookRegistration.unregister();
			eventHookRegistration = null;
		}
		if (basicTopologyManagerComp != null) {
			basicTopologyManagerComp
					.unbindEndpointListener(basicTopologyManagerImpl);
			basicTopologyManagerComp = null;
		}
		if (endpointListenerReg != null) {
			endpointListenerReg.unregister();
			endpointListenerReg = null;
		}
		if (basicTopologyManagerImpl != null) {
			basicTopologyManagerImpl.close();
			basicTopologyManagerImpl = null;
		}
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		this.context = null;
		plugin = null;
	}

}
