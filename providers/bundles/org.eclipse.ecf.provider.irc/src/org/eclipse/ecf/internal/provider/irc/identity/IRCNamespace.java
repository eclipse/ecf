/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.irc.identity;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;

public class IRCNamespace extends org.eclipse.ecf.core.identity.Namespace {

	private static final long serialVersionUID = 1005111581522377553L;

	public static final String IRC_PROTOCOL = "irc"; //$NON-NLS-1$

	private String getProtocolPrefix() {
		return getScheme()+"://";
	}

	public ID createInstance(Object[] args) throws IDCreateException {
		URI newURI = null;
		String s = null;
		try {
			s = (String) args[0];
		} catch (ClassCastException e) {
			throw new IDCreateException("Cannot cast argument " + args[0]
					+ " to String");
		}
		if (!s.startsWith(getProtocolPrefix())) s = getProtocolPrefix()+s;
		try {
			newURI = createURI(s);
//			newURI = new URI(s);
		} catch (URISyntaxException e) {
			throw new IDCreateException("Exception creating URI out of " + s);
		}
		String uriScheme = newURI.getScheme();
		if (uriScheme == null || !uriScheme.equalsIgnoreCase(getScheme())) {
			throw new IDCreateException(newURI
					+ " has invalid protocol.  URI must have protocol "
					+ IRC_PROTOCOL);
		}
		return new IRCID(this, newURI);
	}

	private URI createURI(String s) throws IDCreateException, URISyntaxException{
		URI ret = null;
		String uname = s.substring(getProtocolPrefix().length(),s.indexOf("@"));
		int hostend = s.lastIndexOf("/");
		if(hostend == -1){
			hostend = s.length();
		}
		String host = s.substring(s.indexOf("@")+1,hostend);
		int port = -1;
		int portidx = host.indexOf(":");
		if(portidx >= 0){
			port = Integer.parseInt(host.substring(portidx+1,host.length()));
			host = host.substring(0,portidx);
		}
		String path = s.substring(hostend, s.length());

		ret = new URI(getScheme(),uname,host,port,path,null,null);

		return ret;
	}

	public String getScheme() {
		return IRC_PROTOCOL;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedParameterTypesForCreateInstance()
	 */
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { { String.class } };
	}
}
