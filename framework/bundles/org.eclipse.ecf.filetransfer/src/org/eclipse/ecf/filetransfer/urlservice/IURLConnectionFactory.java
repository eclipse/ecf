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

/**
 * Factory for creating URLConnection instances.
 */
public interface IURLConnectionFactory {
	/**
	 * Create URLConnection instance.
	 * 
	 * @param url the URL to create the URLConnection for.  Will not be null.
	 * @return URLConnection instance that is new URLConnection instance for given URL.  Will not be null.
	 * @throws IOException thrown if some problem creating URLConnection instance
	 */
	public URLConnection createURLConnection(URL url) throws IOException;
	
}
