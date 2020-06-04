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

package org.eclipse.ecf.internal.provider.msn.ui;

import java.net.URI;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlink;
import org.eclipse.jface.text.IRegion;

/**
 * 
 */
public class MSNHyperlink extends AbstractURLHyperlink {

	private static final String ECF_MSN_CONTAINER_NAME = "ecf.msn.msnp"; //$NON-NLS-1$

	/**
	 * Creates a new URL hyperlink.
	 * 
	 * @param region
	 * @param uri
	 */
	public MSNHyperlink(IRegion region, URI uri) {
		super(region, uri);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlink#createConnectWizard()
	 */
	protected IConnectWizard createConnectWizard() {
		return new MSNConnectWizard(getURI().getAuthority());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlink#createContainer()
	 */
	protected IContainer createContainer() throws ContainerCreateException {
		return ContainerFactory.getDefault().createContainer(ECF_MSN_CONTAINER_NAME);
	}

}
