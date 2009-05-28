package org.eclipse.ecf.internal.examples.remoteservices.hello.consumer;

import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.examples.remoteservices.hello.IHello;
import org.eclipse.ecf.osgi.services.distribution.IDistributionConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator, IDistributionConstants, ServiceTrackerCustomizer {

	private BundleContext context;
	private ServiceTracker containerManagerServiceTracker;
	private ServiceTracker helloServiceTracker;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		this.context = context;
		// Create R-OSGi Container
		IContainerManager containerManager = getContainerManagerService();
		containerManager.getContainerFactory().createContainer("ecf.r_osgi.peer");

		helloServiceTracker = new ServiceTracker(context,IHello.class.getName(),this);
		helloServiceTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		helloServiceTracker.close();
		helloServiceTracker = null;
		this.context = null;
	}

	public Object addingService(ServiceReference reference) {
		System.out.println("IHello service proxy being added");
		IHello hello = (IHello) context.getService(reference);
		// Call it
		hello.hello();
		System.out.println("Called hello service");
		return hello;
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
	}

	private IContainerManager getContainerManagerService() {
		if (containerManagerServiceTracker == null) {
			containerManagerServiceTracker = new ServiceTracker(context, IContainerManager.class.getName(),null);
			containerManagerServiceTracker.open();
		}
		return (IContainerManager) containerManagerServiceTracker.getService();
	}


}
