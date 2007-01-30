/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.filetransfer.urlservice;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.osgi.service.url.AbstractURLStreamHandlerService;

/**
 * 
 */
public class UrlStreamHandlerService extends AbstractURLStreamHandlerService {

	private static final String CLASS_ATTRIBUTE = "class";

	protected IConfigurationElement configurationElement;

	public UrlStreamHandlerService(IConfigurationElement element) {
		this.configurationElement = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.url.AbstractURLStreamHandlerService#openConnection(java.net.URL)
	 */
	public URLConnection openConnection(URL u) throws IOException {
		try {
			IURLConnectionFactory connectionFactory = (IURLConnectionFactory) configurationElement
					.createExecutableExtension(CLASS_ATTRIBUTE);
			return connectionFactory.createURLConnection(u);
		} catch (CoreException e) {
			throw new IOException("Exception creating URLConnection instance: "
					+ e.getLocalizedMessage());
		}
	}

}
