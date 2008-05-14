/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.xmpp.identity;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.filetransfer.identity.IFileID;
import org.eclipse.ecf.internal.provider.xmpp.Messages;

/**
 * XMPPFileID for use with the XMPP outgoing file transfer.
 */
public class XMPPFileID extends BaseID implements IFileID {

	private static final long serialVersionUID = 9052434567658554404L;

	XMPPID xmppid;
	String filename;

	public XMPPFileID(XMPPID id, String fn) {
		Assert.isNotNull(id);
		Assert.isNotNull(fn);
		this.xmppid = id;
		this.filename = fn;
	}

	public XMPPID getXMPPID() {
		return xmppid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceToExternalForm()
	 */
	protected String namespaceToExternalForm() {
		return namespace.getScheme() + Namespace.SCHEME_SEPARATOR + xmppid.toExternalForm() + "*" + filename;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceCompareTo(org.eclipse.ecf.core.identity.BaseID)
	 */
	protected int namespaceCompareTo(BaseID o) {
		return getName().compareTo(o.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceEquals(org.eclipse.ecf.core.identity.BaseID)
	 */
	protected boolean namespaceEquals(BaseID o) {
		if (!(o instanceof XMPPFileID))
			return false;
		if (o == null)
			return false;
		final XMPPFileID other = (XMPPFileID) o;
		return this.xmppid.equals(other.xmppid) && this.filename.equals(other.filename);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceGetName()
	 */
	protected String namespaceGetName() {
		return xmppid.getName() + "/" + filename; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceHashCode()
	 */
	protected int namespaceHashCode() {
		return this.xmppid.hashCode() ^ this.filename.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.identity.IFileID#getFilename()
	 */
	public String getFilename() {
		return filename;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.filetransfer.identity.IFileID#getURL()
	 */
	public URL getURL() throws MalformedURLException {
		throw new MalformedURLException(Messages.XMPPFileID_EXCEPTION_FILE_IDS_NOT_URLS);
	}

}
