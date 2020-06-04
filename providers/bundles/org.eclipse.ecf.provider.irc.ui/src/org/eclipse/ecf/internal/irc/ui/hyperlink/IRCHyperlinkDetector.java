/****************************************************************************
 * Copyright (c) 2008 Abner Ballardo and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Abner Ballardo <modlost@modlost.net> - initial API and implementation via bug 197745
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.irc.ui.hyperlink;

import java.net.URI;

import org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlinkDetector;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class IRCHyperlinkDetector extends AbstractURLHyperlinkDetector {

	public static final String IRC_PROTOCOL = "irc"; //$NON-NLS-1$

	public IRCHyperlinkDetector() {
		setProtocols(new String[] {IRC_PROTOCOL});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlinkDetector#createHyperLinksForURI(org.eclipse.jface.text.IRegion, java.net.URI)
	 */
	protected IHyperlink[] createHyperLinksForURI(IRegion region, URI uri) {
		return new IHyperlink[] {new IRCHyperlink(region, uri)};
	}

}
