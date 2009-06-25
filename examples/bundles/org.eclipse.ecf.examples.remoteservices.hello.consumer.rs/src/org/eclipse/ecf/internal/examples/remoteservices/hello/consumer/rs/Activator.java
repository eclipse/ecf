package org.eclipse.ecf.internal.examples.remoteservices.hello.consumer.rs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	public static final String ROSGI_SERVICE_HOST = "r-osgi://localhost:9278";

	private BundleContext context;
	private ServiceTracker containerManagerServiceTracker;
	private IContainer container;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		// Create R-OSGi Container
		IContainerManager containerManager = getContainerManagerService();
		container = containerManager.getContainerFactory().createContainer(
				"ecf.r_osgi.peer");
		// Get remote service container adapter
		IRemoteServiceContainerAdapter containerAdapter = (IRemoteServiceContainerAdapter) container
				.getAdapter(IRemoteServiceContainerAdapter.class);
		// Lookup IRemoteServiceReference
		IRemoteServiceReference[] helloReferences = containerAdapter
				.getRemoteServiceReferences(IDFactory.getDefault().createID(
						container.getConnectNamespace(), ROSGI_SERVICE_HOST),
						IHello.class.getName(), null);
		Assert.isNotNull(helloReferences);
		Assert.isTrue(helloReferences.length > 0);
		// Get remote service for reference
		IRemoteService remoteService = containerAdapter
				.getRemoteService(helloReferences[0]);
		// Get the proxy
		IHello proxy = (IHello) remoteService.getProxy();
		// Finally...call the proxy
		proxy.hello("RemoteService Consumer");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (container != null) {
			container.disconnect();
			container = null;
		}
		if (containerManagerServiceTracker != null) {
			containerManagerServiceTracker.close();
			containerManagerServiceTracker = null;
		}
		this.context = null;
	}

	private IContainerManager getContainerManagerService() {
		if (containerManagerServiceTracker == null) {
			containerManagerServiceTracker = new ServiceTracker(context,
					IContainerManager.class.getName(), null);
			containerManagerServiceTracker.open();
		}
		return (IContainerManager) containerManagerServiceTracker.getService();
	}

}
