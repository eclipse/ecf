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

package org.eclipse.ecf.internal.provider.xmpp.ui.hyperlink;

import java.net.URI;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.internal.provider.xmpp.ui.wizards.XMPPConnectWizard;
import org.eclipse.ecf.internal.provider.xmpp.ui.wizards.XMPPSConnectWizard;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlink;
import org.eclipse.jface.text.IRegion;

/**
 * 
 */
public class XMPPHyperlink extends AbstractURLHyperlink {

	private static final String ECF_XMPP_CONTAINER_NAME = "ecf.xmpp.smack"; //$NON-NLS-1$
	private static final String ECF_XMPPS_CONTAINER_NAME = "ecf.xmpps.smack"; //$NON-NLS-1$

	boolean isXMPPS;
	
	/**
	 * Creates a new URL hyperlink.
	 * 
	 * @param region
	 * @param urlString
	 */
	public XMPPHyperlink(IRegion region, URI uri) {
		super(region, uri);
		isXMPPS = getURI().getScheme()
				.equalsIgnoreCase(XMPPHyperlinkDetector.XMPPS_PROTOCOL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlink#createConnectWizard()
	 */
	protected IConnectWizard createConnectWizard() {
		String auth = getURI().getAuthority();
		if (isXMPPS) return new XMPPSConnectWizard(auth);
		else return new XMPPConnectWizard(auth);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlink#createContainer()
	 */
	protected IContainer createContainer() throws ContainerCreateException {
		return ContainerFactory
				.getDefault()
				.createContainer((isXMPPS) ? ECF_XMPPS_CONTAINER_NAME
						: ECF_XMPP_CONTAINER_NAME);
	}

}
