/****************************************************************************
 * Copyright (c) 2006 Remy Suen, Composent, Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.ui.wizards;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

public class GenericClientConnectWizard extends Wizard implements IConnectWizard {

	private IContainer container;

	private AbstractConnectWizardPage wizardPage;

	private ID targetID;

	private IConnectContext connectContext;

	public void addPages() {
		wizardPage = new GenericClientConnectWizardPage();
		addPage(wizardPage);
	}

	public void init(IWorkbench workbench, IContainer container) {
		this.container = container;
	}

	public ID getTargetID() {
		return targetID;
	}

	public IConnectContext getConnectContext() {
		return connectContext;
	}

	public boolean performFinish() {
		if (wizardPage.shouldRequestPassword()) {
			String password = wizardPage.getPassword();
			if (wizardPage.shouldRequestUsername()) {
				connectContext = ConnectContextFactory
						.createUsernamePasswordConnectContext(wizardPage
								.getUsername(), password);
			} else {
				connectContext = ConnectContextFactory
						.createPasswordConnectContext(password);
			}
		}

		try {
			targetID = IDFactory.getDefault().createID(
					container.getConnectNamespace(), wizardPage.getConnectID());
		} catch (IDCreateException e) {
			// TODO: This needs to be handled properly
			e.printStackTrace();
		}
		return true;
	}

}
