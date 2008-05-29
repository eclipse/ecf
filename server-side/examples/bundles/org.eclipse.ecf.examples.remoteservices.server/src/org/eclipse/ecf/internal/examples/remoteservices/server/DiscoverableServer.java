/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.remoteservices.server;

import java.util.Map;
import java.util.Properties;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.util.DiscoveryProperties;
import org.eclipse.ecf.remoteservice.util.RemoteServiceProperties;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 *
 */
public class DiscoverableServer implements IApplication {

	private static final String ECF_GENERIC_CLIENT = "ecf.generic.client"; //$NON-NLS-1$
	private static final String ECF_GENERIC_SERVER = "ecf.generic.server"; //$NON-NLS-1$
	private static final String ECF_GENERIC_SERVER_ID_NAMESPACE = StringID.class.getName();
	private static final String ECF_GENERIC_SERVER_ID = "ecftcp://localhost:3285/server"; //$NON-NLS-1$

	private IContainer serviceHostContainer;

	private IServiceInfo serviceInfo;

	private IDiscoveryService discoveryService;

	// Parameters
	private String serviceHostContainerType = ECF_GENERIC_SERVER;
	private String serviceHostNamespace = ECF_GENERIC_SERVER_ID_NAMESPACE;
	private String serviceHostID = ECF_GENERIC_SERVER_ID;
	private String clientContainerType = ECF_GENERIC_CLIENT;
	private String clientConnectTarget = ECF_GENERIC_SERVER_ID;
	private String serviceType = Constants.DISCOVERY_SERVICE_TYPE;

	private boolean done = false;

	private String getCompleteServiceType() {
		return "_" + serviceType + "._tcp.local."; //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext ctxt) throws Exception {
		final Map args = ctxt.getArguments();
		initializeFromArguments((String[]) args.get("application.args")); //$NON-NLS-1$

		ID hostID = IDFactory.getDefault().createID(serviceHostNamespace, serviceHostID);
		serviceHostContainer = ContainerFactory.getDefault().createContainer(serviceHostContainerType, hostID);
		final IRemoteServiceContainerAdapter containerAdapter = (IRemoteServiceContainerAdapter) serviceHostContainer.getAdapter(IRemoteServiceContainerAdapter.class);

		discoveryService = Activator.getDefault().getDiscoveryService(5000);

		// register remote service
		final String className = IRemoteEnvironmentInfo.class.getName();
		Properties props = new RemoteServiceProperties(serviceHostContainerType, serviceHostContainer);
		// Add auto registration of remote proxy
		props.put(Constants.AUTOREGISTER_REMOTE_PROXY, "true"); //$NON-NLS-1$

		containerAdapter.registerRemoteService(new String[] {className}, new RemoteEnvironmentInfoImpl(), props);
		System.out.println("Registered remote service " + className); //$NON-NLS-1$

		// then register for discovery
		final String serviceName = System.getProperty("user.name") + System.currentTimeMillis(); //$NON-NLS-1$
		final IServiceID serviceID = ServiceIDFactory.getDefault().createServiceID(discoveryService.getServicesNamespace(), getCompleteServiceType(), serviceName);
		serviceInfo = new ServiceInfo(serviceType, null, 80, serviceID, createServiceProperties(className));
		// register discovery here
		discoveryService.registerService(serviceInfo);
		System.out.println("discovery publish\n\tserviceName=" + serviceID.getServiceName() + "\n\tserviceTypeID=" + serviceID.getServiceTypeID()); //$NON-NLS-1$ //$NON-NLS-2$

		// wait until done
		synchronized (this) {
			while (!done) {
				wait();
			}
		}
		return new Integer(0);
	}

	protected IServiceProperties createServiceProperties(String className) {
		return new ServiceProperties(new DiscoveryProperties(className, clientContainerType, serviceHostNamespace, clientConnectTarget, null, null));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (serviceInfo != null) {
			if (discoveryService != null) {
				try {
					discoveryService.unregisterService(serviceInfo);
				} catch (final ECFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				serviceInfo = null;
				final IContainer container = (IContainer) discoveryService.getAdapter(IContainer.class);
				if (container != null) {
					container.disconnect();
				}
				discoveryService = null;
			}
		}
		if (serviceHostContainer != null) {
			serviceHostContainer.disconnect();
			serviceHostContainer = null;
		}
		synchronized (this) {
			done = true;
			notifyAll();
		}

	}

	private void initializeFromArguments(String[] args) throws Exception {
		if (args == null)
			return;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-serviceHostContainerType")) //$NON-NLS-1$
				serviceHostContainerType = args[++i];
			else if (args[i].equalsIgnoreCase("-serviceHostNamespace")) //$NON-NLS-1$
				serviceHostNamespace = args[++i];
			else if (args[i].equalsIgnoreCase("-serviceHostID")) //$NON-NLS-1$
				serviceHostID = args[++i];
			else if (args[i].equalsIgnoreCase("-clientContainerType")) //$NON-NLS-1$
				clientContainerType = args[++i];
			else if (args[i].equalsIgnoreCase("-clientConnectTarget")) //$NON-NLS-1$
				clientConnectTarget = args[++i];
			else if (args[i].equalsIgnoreCase("-serviceType")) //$NON-NLS-1$
				serviceType = args[++i];
			else {
				usage();
				throw new IllegalArgumentException("Invalid argument"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * 
	 */
	private void usage() {
		System.out.println("usage: eclipse -console [options] -application org.eclipse.ecf.examples.remoteservices.server.remoteServicesServer"); //$NON-NLS-1$
		System.out.println("   options: [-serviceHostContainerType <typename>] default=ecf.generic.server"); //$NON-NLS-1$
		System.out.println("            [-serviceHostID <hostID>] default=ecftcp://localhost:3285/server"); //$NON-NLS-1$
		System.out.println("            [-clientContainerType <typename>] default=ecf.generic.client"); //$NON-NLS-1$
		System.out.println("            [-clientConnectTarget <target>] default=<serviceHostID>"); //$NON-NLS-1$
		System.out.println("            [-serviceType <serviceType>] default=remotesvcs"); //$NON-NLS-1$
	}

}
