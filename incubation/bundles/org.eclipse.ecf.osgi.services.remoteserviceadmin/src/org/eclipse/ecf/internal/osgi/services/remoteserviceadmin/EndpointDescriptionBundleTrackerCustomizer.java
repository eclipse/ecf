/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
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

	private Map<Long, Collection<org.osgi.service.remoteserviceadmin.EndpointDescription>> bundleDescriptionMap = Collections
			.synchronizedMap(new HashMap<Long, Collection<org.osgi.service.remoteserviceadmin.EndpointDescription>>());
	private EndpointDescriptionReader builder = new EndpointDescriptionReader();

	private LocatorServiceListener endpointDescriptionHandler;

	public EndpointDescriptionBundleTrackerCustomizer(
			LocatorServiceListener endpointDescriptionHandler) {
		this.endpointDescriptionHandler = endpointDescriptionHandler;
	}

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
		Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> endpointDescriptions = new ArrayList<org.osgi.service.remoteserviceadmin.EndpointDescription>();
		if (e != null) {
			while (e.hasMoreElements()) {
				org.osgi.service.remoteserviceadmin.EndpointDescription[] eps = handleEndpointDescriptionFile(
						bundle, e.nextElement());
				if (eps != null)
					for (int i = 0; i < eps.length; i++)
						endpointDescriptions.add(eps[i]);
			}
		}
		// finally, handle them
		if (endpointDescriptions.size() > 0) {
			bundleDescriptionMap.put(new Long(bundle.getBundleId()),
					endpointDescriptions);
			for (org.osgi.service.remoteserviceadmin.EndpointDescription ed : endpointDescriptions) {
				endpointDescriptionHandler.handleEndpointDescription(ed, true);
			}
		}
	}

	private org.osgi.service.remoteserviceadmin.EndpointDescription[] handleEndpointDescriptionFile(
			Bundle bundle, URL fileURL) {
		try {
			return builder.readEndpointDescriptions(fileURL.openStream());
		} catch (Exception e) {
			// log
			logError("Exception creating endpoint descriptions from fileURL="
					+ fileURL, e);
			return null;
		}
	}

	private void logError(String message, Throwable t) {
		// XXX todo
		System.err.println(message);
		if (t != null) {
			t.printStackTrace(System.err);
		}
	}

	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
	}

	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		handleRemovedBundle(bundle);
	}

	private void handleRemovedBundle(Bundle bundle) {
		Collection<org.osgi.service.remoteserviceadmin.EndpointDescription> endpointDescriptions = bundleDescriptionMap
				.remove(new Long(bundle.getBundleId()));
		if (endpointDescriptions != null) {
			for (org.osgi.service.remoteserviceadmin.EndpointDescription ed : endpointDescriptions) {
				endpointDescriptionHandler.handleEndpointDescription(ed, false);
			}
		}
	}

	public void close() {
		bundleDescriptionMap.clear();
	}
}
