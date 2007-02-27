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

import org.eclipse.osgi.util.NLS;

public final class BitTorrentMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ecf.internal.provider.bittorrent.BitTorrentMessages"; //$NON-NLS-1$

	public static String TorrentNamespace_InvalidParameter;

	public static String TorrentNamespace_FileIsDirectory;

	public static String TorrentNamespace_CannotReadFile;

	public static String BitTorrentContainer_ReferenceNotTorrentID;

	public static String BitTorrentContainer_CannotWriteToStream;

	public static String BitTorrentContainer_CannotWriteToFile;

	public static String BitTorrentContainer_NullParameter;

	static {
		NLS.initializeMessages(BUNDLE_NAME, BitTorrentMessages.class);
	}

}
