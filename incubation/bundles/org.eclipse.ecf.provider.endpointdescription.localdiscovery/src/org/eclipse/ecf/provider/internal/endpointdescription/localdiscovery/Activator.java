package org.eclipse.ecf.provider.internal.endpointdescription.localdiscovery;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
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
		
		// test code
		testParseServiceDescription();
	}

	private void testParseServiceDescription() {
		try {
			URL file = context.getBundle().getEntry("/endpointdescription1.xml");
			InputStream ins = file.openStream();
			EndpointDescriptionParser parser = new EndpointDescriptionParser();
			parser.parse(ins);
			List<org.eclipse.ecf.provider.internal.endpointdescription.localdiscovery.EndpointDescriptionParser.EndpointDescription> descs = parser.getEndpointDescriptions();
			IServiceInfoFactory serviceInfoFactory = getServiceInfoFactory();
			for(EndpointDescriptionParser.EndpointDescription d: descs) {
				EndpointDescription ed = new EndpointDescription(d.getProperties());
				System.out.println("endpoint description="+ed);
				IServiceInfo serviceInfo = serviceInfoFactory.createServiceInfoForDiscovery(ed);
				System.out.println("serviceInfo="+serviceInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
