package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTrackerCustomizer;

public class EndpointDescriptionBundleTrackerCustomizer implements
		BundleTrackerCustomizer {

	private static final String REMOTESERVICE_MANIFESTHEADER = "Remote-Service";
	private static final String XML_FILE_PATTERN = "*.xml";

	private Map<Long,Collection<org.osgi.service.remoteserviceadmin.EndpointDescription>> bundleDescriptionMap = Collections.synchronizedMap(new HashMap<Long,Collection<org.osgi.service.remoteserviceadmin.EndpointDescription>>());
	private EndpointDescriptionBuilder builder = new EndpointDescriptionBuilder();
	
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		handleAddingBundle(bundle);
		return bundle;
	}

	private void handleAddingBundle(Bundle bundle) {
		BundleContext context = Activator.getContext();
		if (context == null)
			return;
		String remoteServicesHeaderValue = (String) bundle.getHeaders().get(
				REMOTESERVICE_MANIFESTHEADER);
		if (remoteServicesHeaderValue != null) {
			// First parse into comma-separated values
			String[] paths = remoteServicesHeaderValue.split(",");
			if (paths != null)
				for (int i = 0; i < paths.length; i++)
					handleEndpointDescriptionPath(bundle, paths[i]);
		}
	}

	private void handleEndpointDescriptionPath(Bundle bundle,
			String remoteServicesHeaderValue) {
		// if it's empty, ignore
		if ("".equals(remoteServicesHeaderValue))
			return;
		Enumeration<URL> e = null;
		// if it endswith a '/', then scan for *.xml files
		if (remoteServicesHeaderValue.endsWith("/")) {
			e = bundle.findEntries(remoteServicesHeaderValue, XML_FILE_PATTERN,
					false);
		} else {
			// Break into path and filename/pattern
			int lastSlashIndex = remoteServicesHeaderValue.lastIndexOf('/');
			if (lastSlashIndex == -1) {
				// no slash...might be a file name or pattern, assumed to be
				// at root of bundle
				e = bundle.findEntries("/", remoteServicesHeaderValue, false);
			} else {
				String path = remoteServicesHeaderValue.substring(0,
						lastSlashIndex);
				if ("".equals(path)) {
					// path is empty so assume it's root
					path = "/";
				}
				String filePattern = remoteServicesHeaderValue
						.substring(lastSlashIndex + 1);
				e = bundle.findEntries(path, filePattern, false);
			}
		}
		// Now process any found
		Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> bundleDescriptions = new ArrayList<org.osgi.service.remoteserviceadmin.EndpointDescription>();
		if (e != null) {
			while (e.hasMoreElements()) {
				org.osgi.service.remoteserviceadmin.EndpointDescription[] eps = handleEndpointDescriptionFile(bundle, e.nextElement());
				if (eps != null) for(int i=0; i < eps.length; i++) bundleDescriptions.add(eps[i]);
			}
		}
		// finally, publish them
		if (bundleDescriptions.size() > 0) publish(bundle, bundleDescriptions);
	}

	private org.osgi.service.remoteserviceadmin.EndpointDescription[] handleEndpointDescriptionFile(Bundle bundle, URL fileURL) {
		try {
			return builder.createEndpointDescriptions(fileURL.openStream());
		} catch (Exception e) {
			// log
			e.printStackTrace();
			return null;
		}
	}

	private void publish(Bundle bundle, Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> endpointDescriptions) {
		bundleDescriptionMap.put(new Long(bundle.getBundleId()), endpointDescriptions);
		// XXX todo
		System.out.println("publish bundle="+bundle+" endpointDescriptions="+endpointDescriptions);
	}
	
	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
	}

	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		handleRemovedBundle(bundle);
	}

	private void handleRemovedBundle(Bundle bundle) {
		Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> endpointDescriptionsForBundle = bundleDescriptionMap.remove(new Long(bundle.getBundleId()));
		if (endpointDescriptionsForBundle != null) unpublish(bundle, endpointDescriptionsForBundle);
	}

	private void unpublish(Bundle bundle,
			Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> endpointDescriptionsForBundle) {
		// TODO Auto-generated method stub
		System.out.println("unpublish bundle="+bundle+" endpointDescriptions="+endpointDescriptionsForBundle);
	}

	public void close() {
		bundleDescriptionMap.clear();
	}
}
