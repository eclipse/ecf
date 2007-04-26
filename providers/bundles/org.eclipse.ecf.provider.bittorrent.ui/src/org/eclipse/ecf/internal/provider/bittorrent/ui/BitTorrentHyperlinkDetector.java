package org.eclipse.ecf.internal.provider.bittorrent.ui;

import java.net.URI;

import org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class BitTorrentHyperlinkDetector extends AbstractURLHyperlinkDetector {

	public static final String TORRENT_PROTOCOL = "torrent"; //$NON-NLS-1$
	
	public BitTorrentHyperlinkDetector() {
		setProtocols(new String [] { TORRENT_PROTOCOL });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlinkDetector#createHyperLinksForURI(org.eclipse.jface.text.IRegion, java.net.URI)
	 */
	protected IHyperlink[] createHyperLinksForURI(IRegion region, URI uri) {
		return new IHyperlink[] { new BitTorrentHyperlink(region, uri) };
	}	

}
