/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.bittorrent.ui;

import java.net.URI;

import org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class BitTorrentHyperlinkDetector extends AbstractURLHyperlinkDetector {

	public static final String TORRENT_PROTOCOL = "torrent"; //$NON-NLS-1$

	public BitTorrentHyperlinkDetector() {
		setProtocols(new String[] {TORRENT_PROTOCOL});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlinkDetector#createHyperLinksForURI(org.eclipse.jface.text.IRegion, java.net.URI)
	 */
	protected IHyperlink[] createHyperLinksForURI(IRegion region, URI uri) {
		return new IHyperlink[] {new BitTorrentHyperlink(region, uri)};
	}

}
