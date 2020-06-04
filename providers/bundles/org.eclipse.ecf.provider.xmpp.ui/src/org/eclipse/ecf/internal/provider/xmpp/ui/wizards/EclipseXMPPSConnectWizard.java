/****************************************************************************
 * Copyright (c) 2008 Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.xmpp.ui.wizards;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

public final class EclipseXMPPSConnectWizard extends XMPPConnectWizard {

	public EclipseXMPPSConnectWizard() {
		super();
	}

	public EclipseXMPPSConnectWizard(String uri) {
		super(uri);
	}

	public void addPages() {
		page = new EclipseXMPPSConnectWizardPage(usernameAtHost);
		addPage(page);
	}

	public void init(IWorkbench workbench, IContainer container) {
		super.init(workbench, container);

		setWindowTitle("Connect to Eclipse IM");
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		try {
			this.container = ContainerFactory.getDefault().createContainer("ecf.xmpps.smack");
		} catch (final ContainerCreateException e) {
			// None
		}

		setWindowTitle("Connect to Eclipse IM");
	}
}
