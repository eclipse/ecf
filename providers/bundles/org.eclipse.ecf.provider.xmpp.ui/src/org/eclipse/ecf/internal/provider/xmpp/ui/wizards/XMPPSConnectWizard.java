/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.xmpp.ui.wizards;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.internal.provider.xmpp.ui.Messages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;


public final class XMPPSConnectWizard extends XMPPConnectWizard {

	public XMPPSConnectWizard() {
		super();
	}
	
	public XMPPSConnectWizard(String uri) {
		super(uri);
	}
	
	public void addPages() {
		page = new XMPPSConnectWizardPage(usernameAtHost);
		addPage(page);
	}

	public void init(IWorkbench workbench, IContainer container) {
		super.init(workbench, container);

		setWindowTitle(Messages.XMPPSConnectWizard_WIZARD_TITLE);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		try {
			this.container = ContainerFactory.getDefault().createContainer("ecf.xmpps.smack");
		} catch (ContainerCreateException e) {
			// None
		}

		setWindowTitle(Messages.XMPPSConnectWizard_WIZARD_TITLE);
	}
}
