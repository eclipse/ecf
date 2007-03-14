/*******************************************************************************
 * Copyright (c) 2006, 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.bittorrent;

import org.eclipse.bittorrent.TorrentConfiguration;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public final class BitTorrentProviderPlugin extends Plugin {

	static final String CONTAINER_ID = "ecf.filetransfer.bittorrent"; //$NON-NLS-1$

	static final String NAMESPACE_ID = "ecf.bittorrent"; //$NON-NLS-1$

	public void start(BundleContext context) throws Exception {
		super.start(context);
		TorrentConfiguration.setConfigurationPath(getStateLocation().toFile());
	}

}
