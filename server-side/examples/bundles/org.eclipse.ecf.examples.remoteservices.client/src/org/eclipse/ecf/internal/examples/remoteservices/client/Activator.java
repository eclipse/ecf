package org.eclipse.ecf.internal.examples.remoteservices.client;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static final String ECF_GENERIC_CLIENT = "ecf.generic.client";

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.examples.remoteservices.client";

	// The shared instance
	private static Activator plugin;

	private IContainer serviceHostContainer;

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
		serviceHostContainer = ContainerFactory.getDefault().createContainer(ECF_GENERIC_CLIENT);
		serviceHostContainer.getAdapter(IRemoteServiceContainerAdapter.class);
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

	public IContainer getContainer() {
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
