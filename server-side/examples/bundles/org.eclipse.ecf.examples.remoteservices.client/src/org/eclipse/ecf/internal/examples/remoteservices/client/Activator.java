package org.eclipse.ecf.internal.examples.remoteservices.client;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.examples.remoteservices.client";

	private static final String DEFAULT_CONNECT_TARGET = "ecftcp://ecf.eclipse.org:3282/server";

	// The shared instance
	private static Activator plugin;

	private IContainer serviceHostContainer;

	private IRemoteServiceContainerAdapter getRemoteServiceContainerAdapter(IContainer container) {
		return (IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class);
	}

	private void createAndConnectServiceHostContainer() {
		try {
			serviceHostContainer = ContainerFactory.getDefault().createContainer("ecf.generic.client");
			final ID targetID = IDFactory.getDefault().createID(serviceHostContainer.getConnectNamespace(), DEFAULT_CONNECT_TARGET);
			serviceHostContainer.addListener(new IContainerListener() {
				public void handleEvent(IContainerEvent event) {
					// TODO Auto-generated method stub
					System.out.println("serviceHostContainerEvent(" + event + ")");
				}
			});
			final IRemoteServiceContainerAdapter containerAdapter = getRemoteServiceContainerAdapter(serviceHostContainer);
			containerAdapter.addRemoteServiceListener(new IRemoteServiceListener() {

				public void handleServiceEvent(IRemoteServiceEvent event) {
					System.out.println("remoteServiceEvent(" + event + ")");
				}
			});
			serviceHostContainer.connect(targetID, null);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		createAndConnectServiceHostContainer();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		if (serviceHostContainer != null) {
			serviceHostContainer.disconnect();
			serviceHostContainer = null;
		}
	}

	public IContainer getConnectedContainer() {
		return serviceHostContainer;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
