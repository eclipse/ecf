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

import java.security.InvalidParameterException;
import java.util.Map;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
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

	private IContainer serviceHostContainer;

	private IServiceInfo serviceInfo;

	private IDiscoveryContainerAdapter discovery;

	private String containerType;
	private String connectTarget;
	private String connectPassword;
	private String serviceType;

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

		serviceHostContainer = ContainerFactory.getDefault().createContainer(containerType);
		final ID targetID = IDFactory.getDefault().createID(serviceHostContainer.getConnectNamespace(), connectTarget);
		final IConnectContext connectContext = (connectPassword == null) ? null : ConnectContextFactory.createPasswordConnectContext(connectPassword);
		serviceHostContainer.connect(targetID, connectContext);

		discovery = Activator.getDefault().getDiscoveryService(5000);

		final IRemoteServiceContainerAdapter containerAdapter = (IRemoteServiceContainerAdapter) serviceHostContainer.getAdapter(IRemoteServiceContainerAdapter.class);
		// register remote service
		final String className = IRemoteEnvironmentInfo.class.getName();
		containerAdapter.registerRemoteService(new String[] {className}, new RemoteEnvironmentInfoImpl(), new RemoteServiceProperties(containerType, serviceHostContainer));
		System.out.println("Registered remote service " + className); //$NON-NLS-1$

		// then register for discovery
		final String serviceName = System.getProperty("user.name") + System.currentTimeMillis(); //$NON-NLS-1$
		final IServiceID serviceID = ServiceIDFactory.getDefault().createServiceID(discovery.getServicesNamespace(), getCompleteServiceType(), serviceName);
		serviceInfo = new ServiceInfo(serviceType, null, 80, serviceID, new ServiceProperties(new DiscoveryProperties(className, containerType, serviceHostContainer)));
		// register discovery here
		discovery.registerService(serviceInfo);
		System.out.println("discovery publish\n\tserviceName=" + serviceID.getServiceName() + "\n\tserviceTypeID=" + serviceID.getServiceTypeID()); //$NON-NLS-1$ //$NON-NLS-2$

		// wait until done
		synchronized (this) {
			while (!done) {
				wait();
			}
		}
		return new Integer(0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		if (serviceInfo != null) {
			if (discovery != null) {
				try {
					discovery.unregisterService(serviceInfo);
				} catch (final ECFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				serviceInfo = null;
				final IContainer container = (IContainer) discovery.getAdapter(IContainer.class);
				if (container != null) {
					container.disconnect();
				}
				discovery = null;
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
			if (!args[i].startsWith("-")) { //$NON-NLS-1$
				connectTarget = args[i++];
			} else {
				if (args[i - 1].equalsIgnoreCase("-containerType")) //$NON-NLS-1$
					containerType = args[++i];
				else if (args[i - 1].equalsIgnoreCase("-connectPassword")) //$NON-NLS-1$
					connectPassword = args[++i];
				else if (args[i - 1].equalsIgnoreCase("-serviceType")) //$NON-NLS-1$
					serviceType = args[++i];
			}
		}
		if (connectTarget == null) {
			usage();
			throw new InvalidParameterException("connectTarget cannot be null"); //$NON-NLS-1$
		}
		if (containerType == null)
			containerType = ECF_GENERIC_CLIENT;
		if (serviceType == null)
			serviceType = Constants.DISCOVERY_SERVICE_TYPE;
	}

	/**
	 * 
	 */
	private void usage() {
		System.out.println("usage: eclipse -console [options] -application org.eclipse.ecf.examples.remoteservices.server.remoteServicesServer <connectTarget>"); //$NON-NLS-1$
		System.out.println("   options: [-containerType <typename>] default=ecf.generic.client"); //$NON-NLS-1$
		System.out.println("            [-connectPassword <password>] default=none"); //$NON-NLS-1$
		System.out.println("            [-serviceType <serviceType>] default=remotesvcs"); //$NON-NLS-1$
	}

}
