/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.bittorrent;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;

public final class BitTorrentURLStreamHandlerService extends
		AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL u) throws IOException {
		return new BitTorrentConnection(u);
	}

}
