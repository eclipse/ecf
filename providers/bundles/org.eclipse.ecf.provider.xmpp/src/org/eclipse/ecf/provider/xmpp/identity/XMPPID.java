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
package org.eclipse.ecf.provider.xmpp.identity;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.provider.xmpp.Messages;
import org.eclipse.ecf.internal.provider.xmpp.smack.ECFConnection;
import org.eclipse.ecf.presence.im.IChatID;

public class XMPPID extends BaseID implements IChatID {

	private static final long serialVersionUID = 3257289140701049140L;
	public static final char USER_HOST_DELIMITER = '@';
	public static final char PORT_DELIMITER = ':';
	public static final char PATH_DELIMITER = '/';

	URI uri;
	String username;
	String hostname;
	String resourcename = ECFConnection.CLIENT_TYPE;
	int port = -1;

	protected static String fixEscape(String src) {
		if (src == null)
			return null;
		return src.replaceAll("%", "%25"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public XMPPID(Namespace namespace, String unamehost) throws URISyntaxException {
		super(namespace);
		unamehost = fixEscape(unamehost);
		if (unamehost == null)
			throw new URISyntaxException(unamehost, Messages.XMPPID_EXCEPTION_XMPPID_USERNAME_NOT_NULL);
		// Handle parsing of user@host/resource string
		int atIndex = unamehost.lastIndexOf(USER_HOST_DELIMITER);
		if (atIndex == -1)
			throw new URISyntaxException(unamehost, Messages.XMPPID_EXCEPTION_HOST_PORT_NOT_VALID);
		username = unamehost.substring(0, atIndex);
		final String remainder = unamehost.substring(atIndex + 1);
		// Handle parsing of host:port
		atIndex = remainder.lastIndexOf(PORT_DELIMITER);
		if (atIndex != -1) {
			try {
				final int slashLoc = remainder.indexOf(PATH_DELIMITER);
				if (slashLoc != -1)
					port = Integer.parseInt(remainder.substring(atIndex + 1, slashLoc));
				else
					port = Integer.parseInt(remainder.substring(atIndex + 1));
			} catch (final NumberFormatException e) {
				throw new URISyntaxException(unamehost, Messages.XMPPID_EXCEPTION_INVALID_PORT);
			}
			hostname = remainder.substring(0, atIndex);
		}
		atIndex = remainder.indexOf(PATH_DELIMITER);
		if (atIndex != -1) {
			if (hostname == null)
				hostname = remainder.substring(0, atIndex);
			resourcename = PATH_DELIMITER + remainder.substring(atIndex + 1);
		} else {
			resourcename = PATH_DELIMITER + ECFConnection.CLIENT_TYPE;
		}
		if (hostname == null)
			hostname = remainder;
		uri = new URI(namespace.getScheme(), username, hostname, port, resourcename, null, null);
	}

	protected int namespaceCompareTo(BaseID o) {
		return getName().compareTo(o.getName());
	}

	protected boolean namespaceEquals(BaseID o) {
		if (!(o instanceof XMPPID)) {
			return false;
		}
		final XMPPID other = (XMPPID) o;
		return getUsernameAtHost().equals(other.getUsernameAtHost());
	}

	protected String namespaceGetName() {
		return getUsernameAtHost();
	}

	protected int namespaceHashCode() {
		return getUsernameAtHost().hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.BaseID#namespaceToExternalForm()
	 */
	protected String namespaceToExternalForm() {
		return uri.toASCIIString();
	}

	public String getUsername() {
		return username;
	}

	public String getHostname() {
		return hostname;
	}

	public String getResourceName() {
		return resourcename;
	}

	public int getPort() {
		return port;
	}

	public String getUsernameAtHost() {
		return getUsername() + USER_HOST_DELIMITER + getHostname();
	}

	public String getFQName() {
		return getUsernameAtHost() + ((getPort() == -1) ? "" : ":" + getPort()) + getResourceName();
	}

	public String toString() {
		final StringBuffer sb = new StringBuffer("XMPPID["); //$NON-NLS-1$
		sb.append(uri.toString()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}

	public Object getAdapter(Class clazz) {
		if (clazz.isInstance(this)) {
			return this;
		} else
			return super.getAdapter(clazz);
	}
}
