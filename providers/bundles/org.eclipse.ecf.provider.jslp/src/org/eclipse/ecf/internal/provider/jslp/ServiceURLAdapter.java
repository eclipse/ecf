/*******************************************************************************
 * Copyright (c) 2007 Versant Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.provider.jslp;

import ch.ethz.iks.slp.ServiceURL;
import java.net.URI;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.provider.jslp.identity.JSLPNamespace;

/**
 * Adapts jSLP's ServiceURL to an ECF's IServiceID and vice versa
 */
public class ServiceURLAdapter {

	private URI uri;
	private IServiceID serviceID;

	public ServiceURLAdapter(final ServiceURL aServiceURL, final String aServiceName) {
		this(aServiceURL, aServiceName, new String[0]);
	}

	public ServiceURLAdapter(final ServiceURL aServiceURL, final String aServiceName, final String[] scopes) {
		Assert.isNotNull(aServiceURL);
		Assert.isNotNull(scopes);
		setIServiceID(aServiceURL, aServiceName, scopes);
		setURI(aServiceURL);
	}

	private void setURI(final ServiceURL aServiceURL) {
		final StringBuffer buf = new StringBuffer();
		String protocol = aServiceURL.getProtocol();
		if (protocol == null) {
			protocol = "unknown"; //$NON-NLS-1$
		}
		buf.append(protocol);
		buf.append("://"); //$NON-NLS-1$
		final String userInfo = aServiceURL.getUserInfo();
		if (!"".equals(userInfo)) { //$NON-NLS-1$
			buf.append(userInfo);
			buf.append('@');
		}
		buf.append(aServiceURL.getHost());
		buf.append(':');
		buf.append(aServiceURL.getPort());
		buf.append(aServiceURL.getURLPath());
		uri = URI.create(buf.toString());
	}

	private void setIServiceID(final ServiceURL aServiceURL, final String aServiceName, final String[] scopes) {
		final Namespace namespace = IDFactory.getDefault().getNamespaceByName(JSLPNamespace.NAME);
		serviceID = (IServiceID) namespace.createInstance(new Object[] {aServiceURL, aServiceName, scopes});
	}

	/**
	 * @return URI
	 */
	public URI getURI() {
		return uri;
	}

	/**
	 * @return IServiceID
	 */
	public IServiceID getIServiceID() {
		return serviceID;
	}
}
