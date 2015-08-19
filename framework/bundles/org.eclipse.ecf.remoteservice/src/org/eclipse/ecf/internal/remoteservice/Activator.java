/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.remoteservice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.SystemLogService;
import org.eclipse.ecf.remoteservice.IRemoteServiceProxyCreator;
import org.eclipse.ecf.remoteservice.RemoteServiceNamespace;
import org.eclipse.ecf.remoteservice.provider.IRemoteServiceDistributionProvider;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.remoteservice"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private BundleContext context;

	private ServiceTracker logServiceTracker = null;

	private LogService logService = null;

	private ServiceRegistration remoteServiceProxyCreator;

	private RemoteServiceNamespace remoteServiceNamespace;

	private ServiceTracker<IRemoteServiceDistributionProvider, IRemoteServiceDistributionProvider> distributionProviderTracker;

	class RSDPRegistrations {
		private ServiceRegistration<ContainerTypeDescription> ctdSR;
		private ServiceRegistration<Namespace> nsSR;

		RSDPRegistrations(ServiceRegistration<ContainerTypeDescription> ctdSR, ServiceRegistration<Namespace> nsSR) {
			this.ctdSR = ctdSR;
			this.nsSR = nsSR;
		}

		void unregister(ServiceRegistration<?> reg) {
			try {
				reg.unregister();
			} catch (Exception e) {
				log(new Status(IStatus.ERROR, PLUGIN_ID, "Could not unregister serviceReg=" + this.ctdSR, e)); //$NON-NLS-1$
			}
		}

		synchronized void unregisterServices() {
			if (this.ctdSR != null) {
				unregister(this.ctdSR);
				this.ctdSR = null;
			}
			if (this.nsSR != null) {
				unregister(this.nsSR);
				this.nsSR = null;
			}
		}
	}

	Map<ServiceReference<IRemoteServiceDistributionProvider>, RSDPRegistrations> svcRefToDSDPRegMap;

	private ServiceTrackerCustomizer<IRemoteServiceDistributionProvider, IRemoteServiceDistributionProvider> distributionProviderCustomizer = new ServiceTrackerCustomizer<IRemoteServiceDistributionProvider, IRemoteServiceDistributionProvider>() {

		public IRemoteServiceDistributionProvider addingService(ServiceReference<IRemoteServiceDistributionProvider> reference) {
			BundleContext bundleContext = getContext();
			IRemoteServiceDistributionProvider dProvider = bundleContext.getService(reference);
			if (dProvider != null) {
				ServiceRegistration<ContainerTypeDescription> ctdSR = dProvider.registerContainerTypeDescription(bundleContext);
				ServiceRegistration<Namespace> nsSR = dProvider.registerNamespace(bundleContext);
				if (ctdSR != null)
					svcRefToDSDPRegMap.put(reference, new RSDPRegistrations(ctdSR, nsSR));
			}
			return dProvider;
		}

		public void modifiedService(ServiceReference<IRemoteServiceDistributionProvider> reference, IRemoteServiceDistributionProvider service) {
			// nothing
		}

		public void removedService(ServiceReference<IRemoteServiceDistributionProvider> reference, IRemoteServiceDistributionProvider service) {
			RSDPRegistrations regs = svcRefToDSDPRegMap.remove(reference);
			if (regs != null)
				regs.unregisterServices();
		}

	};

	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	public BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext c) throws Exception {
		// nothing to do
		this.context = c;
		// Register default IRemoteServiceProxyCreator
		Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_RANKING, Integer.MIN_VALUE);
		this.remoteServiceProxyCreator = this.context.registerService(new String[] {IRemoteServiceProxyCreator.class.getName()}, new IRemoteServiceProxyCreator() {
			public Object createProxy(ClassLoader classloader, Class[] interfaces, InvocationHandler handler) {
				return Proxy.newProxyInstance(classloader, interfaces, handler);
			}
		}, props);
		// Setup namespace
		this.remoteServiceNamespace = new RemoteServiceNamespace(RemoteServiceNamespace.NAME, "remote service namespace"); //$NON-NLS-1$
		IDFactory.getDefault().addNamespace(remoteServiceNamespace);
		svcRefToDSDPRegMap = Collections.synchronizedMap(new HashMap<ServiceReference<IRemoteServiceDistributionProvider>, RSDPRegistrations>());

		distributionProviderTracker = new ServiceTracker<IRemoteServiceDistributionProvider, IRemoteServiceDistributionProvider>(getContext(), IRemoteServiceDistributionProvider.class, distributionProviderCustomizer);
		distributionProviderTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext c) throws Exception {
		if (distributionProviderTracker != null) {
			distributionProviderTracker.close();
			distributionProviderTracker = null;
		}
		if (this.remoteServiceProxyCreator != null) {
			this.remoteServiceProxyCreator.unregister();
			this.remoteServiceProxyCreator = null;
		}
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
			logService = null;
		}
		// Remote namespace
		if (remoteServiceNamespace != null) {
			IDFactory.getDefault().removeNamespace(remoteServiceNamespace);
			remoteServiceNamespace = null;
		}
		this.context = null;
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public synchronized static Activator getDefault() {
		if (plugin == null) {
			plugin = new Activator();
		}
		return plugin;
	}

	protected LogService getLogService() {
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context, LogService.class.getName(), null);
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
			logService.log(LogHelper.getLogCode(status), LogHelper.getLogMessage(status), status.getException());
	}

}
