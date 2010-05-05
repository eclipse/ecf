/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.discovery.local;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.service.log.LogService;
import org.xml.sax.SAXException;

public class ServiceDescriptionPublisher implements
		IServiceEndpointDescriptionPublisher {

	private FileBasedDiscoveryImpl discovery;
	private ServiceDescriptionParser sdp;
	private Object discoveryLock = new Object();

	public ServiceDescriptionPublisher(FileBasedDiscoveryImpl discovery) {
		this.discovery = discovery;
		this.sdp = new ServiceDescriptionParser();
	}

	private void logException(String message, Throwable t) {
		Activator a = Activator.getDefault();
		if (a != null) {
			LogService logService = a.getLogService();
			if (logService != null)
				logService.log(LogService.LOG_ERROR, message, t);
		}
	}

	private Collection getServiceEndpointDescriptions(
			InputStream serviceDescriptionStream) throws IOException {
		try {
			return sdp.load(serviceDescriptionStream);
		} catch (IOException e) {
			logException("IOException publishing serviceDescriptionStream", e);
			throw e;
		} catch (ParserConfigurationException e) {
			logException(
					"Parser exception publishing serviceDescriptionStream", e);
			throw new IOException(
					"Parser exception publishing serviceDescriptionStream: "
							+ e.getMessage());
		} catch (SAXException e) {
			logException(
					"Parser exception publishing serviceDescriptionStream", e);
			throw new IOException(
					"SAX exception publishing serviceDescriptionStream: "
							+ e.getMessage());
		} finally {
			serviceDescriptionStream.close();
		}
	}

	public void publishServiceDescription(InputStream serviceDescriptionStream)
			throws IOException {
		Collection serviceDescriptions = getServiceEndpointDescriptions(serviceDescriptionStream);
		if (serviceDescriptions != null) {
			for (Iterator i = serviceDescriptions.iterator(); i.hasNext();) {
				publishServiceDescription((ServiceEndpointDescriptionImpl) i
						.next());
			}
		}
	}

	public void unpublishServiceDescription(InputStream serviceDescriptionStream)
			throws IOException {
		Collection serviceDescriptions = getServiceEndpointDescriptions(serviceDescriptionStream);
		if (serviceDescriptions != null) {
			for (Iterator i = serviceDescriptions.iterator(); i.hasNext();) {
				unpublishServiceDescription((ServiceEndpointDescriptionImpl) i
						.next());
			}
		}
	}

	public void publishServiceDescription(
			ServiceEndpointDescriptionImpl serviceEndpointDescription) {
		synchronized (discoveryLock) {
			if (discovery != null)
				discovery.publishService(serviceEndpointDescription);
		}
	}

	public void unpublishServiceDescription(
			ServiceEndpointDescriptionImpl serviceEndpointDescription) {
		synchronized (discoveryLock) {
			if (discovery != null)
				discovery.unpublishService(serviceEndpointDescription);
		}
	}

	public void close() {
		synchronized (discoveryLock) {
			this.discovery = null;
		}
	}

}
