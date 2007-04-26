package org.eclipse.ecf.internal.irc.ui.hyperlink;

import java.net.URI;

import org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class IRCHyperlinkDetector extends AbstractURLHyperlinkDetector {

	public static final String IRC_PROTOCOL = "irc"; //$NON-NLS-1$
	
	public IRCHyperlinkDetector() {
		setProtocols(new String [] { IRC_PROTOCOL });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlinkDetector#createHyperLinksForURI(org.eclipse.jface.text.IRegion, java.net.URI)
	 */
	protected IHyperlink[] createHyperLinksForURI(IRegion region, URI uri) {
		return new IHyperlink[] { new IRCHyperlink(region, uri) };
	}	

}
