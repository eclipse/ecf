/* 
 * Copyright (c) 2009 Siemens Enterprise Communications GmbH & Co. KG, 
 * Germany. All rights reserved.
 *
 * Siemens Enterprise Communications GmbH & Co. KG is a Trademark Licensee 
 * of Siemens AG.
 *
 * This material, including documentation and any related computer programs,
 * is protected by copyright controlled by Siemens Enterprise Communications 
 * GmbH & Co. KG and its licensors. All rights are reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.ecf.osgi.services.discovery.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescription;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.xml.sax.SAXException;

public class BundleTrackerImpl implements BundleTrackerCustomizer {

	private FileBasedDiscoveryImpl discovery = null;

	private static final String REMOTESERVICE_LOCATION = "OSGI-INF"
			+ File.separator + "remote-service";

	private static final String REMOTESERVICE_MANIFESTHEADER = "Remote-Service";

	// stores the bundle ids of the bundles that are checked for the static
	// discovery informations
	private Collection checkedBundles = Collections
			.synchronizedList(new ArrayList());

	private Map publishedServicesPerBundle = Collections
			.synchronizedMap(new HashMap());

	/**
	 * 
	 * @param disco
	 *            the FileBasedDiscovery implementation that publishes and
	 *            unpublishes file based seds.
	 */
	public BundleTrackerImpl(FileBasedDiscoveryImpl disco) {
		discovery = disco;
	}

	/**
	 * @see org.osgi.util.tracker.BundleTrackerCustomizer#addingBundle(org.osgi.framework.Bundle,
	 *      org.osgi.framework.BundleEvent)
	 */
	public Object addingBundle(Bundle bundle, BundleEvent event) {
		FileBasedDiscoveryImpl.log(LogService.LOG_INFO, "Adding bundle "
				+ bundle.getSymbolicName() + " with event " + event);
		checkedBundles.add(String.valueOf(bundle.getBundleId()));
		return checkBundleAndPublishServices(bundle);
	}

	/**
	 * @param bundle
	 * @return
	 */
	private Bundle checkBundleAndPublishServices(Bundle bundle) {
		Collection /* <URL> */remoteServiceFiles = null;
		Collection /* <ServiceEndpointDescription> */publishedServices = null;
		if ((remoteServiceFiles = getRemoteServiceInformationFilesFromBundle(bundle))
				.size() == 0) {
			return null;
		}
		Iterator/* <URL> */it = remoteServiceFiles.iterator();
		publishedServices = new ArrayList();
		while (it.hasNext()) {
			// each file may contain more than ServiceDescription
			Collection seds = createSEDsFromFile((URL) it.next());
			Iterator /* <ServiceEndpointDescription> */sedIterator = seds
					.iterator();
			while (sedIterator.hasNext()) {
				// publish each of them
				ServiceEndpointDescription next = (ServiceEndpointDescription) sedIterator
						.next();
				discovery.publishService(next);
				publishedServices.add(next);
			}
		}
		publishedServicesPerBundle.put(bundle, publishedServices);
		return bundle;
	}

	/**
	 * @see org.osgi.util.tracker.BundleTrackerCustomizer#modifiedBundle(org.osgi.framework.Bundle,
	 *      org.osgi.framework.BundleEvent, java.lang.Object)
	 */
	public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
		// ignored

	}

	/**
	 * @see org.osgi.util.tracker.BundleTrackerCustomizer#removedBundle(org.osgi.framework.Bundle,
	 *      org.osgi.framework.BundleEvent, java.lang.Object)
	 */
	public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
		FileBasedDiscoveryImpl.log(LogService.LOG_INFO, "Removing bundle "
				+ bundle.getSymbolicName() + " with event " + event);
		Collection publishedServices = (Collection) publishedServicesPerBundle
				.get(bundle);
		if (publishedServices != null) {
			Iterator it = publishedServices.iterator();
			while (it.hasNext()) {
				discovery.unpublishService((ServiceEndpointDescription) it
						.next());
			}
		}
		publishedServicesPerBundle.remove(bundle);
		checkedBundles.remove(String.valueOf(bundle.getBundleId()));
	}

	/**
	 * 
	 * @param bundle
	 *            the bundle to check
	 * @return the URL list of Distributed OSGi compliant xml files. The list is
	 *         empty if none are found.
	 */
	private Collection getRemoteServiceInformationFilesFromBundle(
			final Bundle bundle) {
		Collection result = new ArrayList();
		String remote_services = (String) bundle.getHeaders().get(
				REMOTESERVICE_MANIFESTHEADER);
		if (remote_services != null) {
			// the default has been overwritten
			StringTokenizer tokenizer = new StringTokenizer(remote_services,
					",");
			while (tokenizer.hasMoreElements()) {
				handleRemoteService(bundle, result, tokenizer);
			}
		} else {
			// default location
			Enumeration bundleEntries = bundle.findEntries(
					REMOTESERVICE_LOCATION, "*.xml", true);
			if (bundleEntries != null) {
				while (bundleEntries.hasMoreElements()) {
					result.add(bundleEntries.nextElement());
				}
			}
		}
		return result;
	}

	/**
	 * @param bundle
	 * @param result
	 * @param tokenizer
	 */
	private void handleRemoteService(final Bundle bundle, Collection result,
			StringTokenizer tokenizer) {
		String token = tokenizer.nextToken().trim();
		// fix for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=325664
		int lastSlash = token.lastIndexOf("/");
		// We set the path to '/' is not present at all in token (lastSlash == -1)
		// or if the slash is first char (lastSlash==0)
		String path = (lastSlash <= 0)?"/":token.substring(0,lastSlash);
		int start = path.indexOf("${");
		if (start >= 0) {
			int end = path.indexOf("}");
			String substring = path.substring(start + 2, end);
			String property = System.getProperty(substring);
			path = path.replaceAll("\\$\\{" + substring + "\\}", property);
		}
		// Also be sure to use lastSlash to get files
		String files = token.substring(lastSlash+1,
				token.length());
		Enumeration enumeration = bundle.findEntries(path, files, false);
		if (enumeration == null) {
			// that was maybe an absolute file path so lets check that.
			File f = new File(path + File.separator + files);
			if (f.isFile() && f.exists()) {
				try {
					// we have file given
					result.add(f.toURL());
				} catch (MalformedURLException e) {
					// ignore that location
				}
			} else if (f.isDirectory() && f.exists()) {
				// if it is a directory and it does exist
				addFilesToResult(result, f);
			} else if (new File(path).isDirectory()) {
				// if we have a directory extended with a wildcard file
				// selector given
				File directory = new File(path);
				if (directory.exists() && files.equals("*.xml")) {
					addFilesToResult(result, directory);
				}
			}
		} else {
			while (enumeration.hasMoreElements()) {
				result.add(enumeration.nextElement());
			}
		}
	}

	/**
	 * @param result
	 * @param f
	 */
	private void addFilesToResult(Collection result, File f) {
		File[] filesInTheDirectory = f.listFiles(new FilenameFilter() {

			public boolean accept(File directory, String fileName) {
				return (fileName.endsWith(".xml")) ? true : false;
			}
		});
		for (int i = 0; i < filesInTheDirectory.length; i++) {
			try {
				result.add(filesInTheDirectory[i].toURL());
			} catch (MalformedURLException e) {
				// ignore the file and go on with the next one
			}
		}
	}

	/**
	 * 
	 * @param file
	 *            the xml file containing valid Distributed OSGi service
	 *            information
	 * @return a ServiceEndpointDescription object containing the information
	 *         from the file
	 */
	private Collection/* <ServiceEndpointDescription> */createSEDsFromFile(
			final URL file) {
		try {
			InputStream inputStream = file.openStream();
			return new ServiceDescriptionParser().load(inputStream);
		} catch (FileNotFoundException e) {
			log(file, e);
		} catch (ParserConfigurationException e) {
			log(file, e);
		} catch (SAXException e) {
			log(file, e);
		} catch (IOException e) {
			log(file, e);
		}
		return new ArrayList();
	}

	/**
	 * @param file
	 * @param e
	 */
	private void log(URL file, Exception e) {
		e.printStackTrace();
		FileBasedDiscoveryImpl.log(LogService.LOG_ERROR,
				"Error during loading and reading of service descriptions from file "
						+ file.getFile(), e);
	}
}
