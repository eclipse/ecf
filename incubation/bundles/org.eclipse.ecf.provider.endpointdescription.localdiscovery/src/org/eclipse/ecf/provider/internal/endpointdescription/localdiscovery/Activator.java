package org.eclipse.ecf.provider.internal.endpointdescription.localdiscovery;

import javax.xml.parsers.SAXParserFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Activator instance;

	private ServiceTracker parserTracker;
	
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		instance = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		closeSAXParserTracker();
		Activator.context = null;
		instance = null;
	}

	public static Activator getDefault() {
		return instance;
	}

	public synchronized SAXParserFactory getSAXParserFactory() {
		if (instance == null) return null;
		if (parserTracker == null) {
			parserTracker = new ServiceTracker(context,SAXParserFactory.class.getName(),null);
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
}
