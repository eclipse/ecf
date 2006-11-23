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
package org.eclipse.ecf.provider.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.IExceptionHandler;
import org.eclipse.ecf.internal.provider.ui.Activator;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.ecf.ui.actions.AsynchContainerConnectAction;
import org.eclipse.ecf.ui.dialogs.ContainerConnectErrorDialog;
import org.eclipse.ecf.ui.wizards.AbstractConnectWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

public class GenericClientContainerConnectWizard extends Wizard implements
		IConnectWizard {

	protected static final int CONNECT_ERROR_CODE = 7777;

	private IContainer container;

	private AbstractConnectWizardPage wizardPage;

	private ID targetID;

	private IConnectContext connectContext;

	private IWorkbench workbench;

	private Shell shell;

	public void addPages() {
		wizardPage = new GenericClientContainerConnectWizardPage();
		addPage(wizardPage);
	}

	public void init(IWorkbench workbench, IContainer container) {
		this.container = container;
		this.workbench = workbench;
		this.shell = this.workbench.getActiveWorkbenchWindow().getShell();
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
			return false;
		}

		new AsynchContainerConnectAction(this.container, this.targetID,
				this.connectContext, new IExceptionHandler() {
					public IStatus handleException(final Throwable exception) {
						if (exception != null) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									new ContainerConnectErrorDialog(
											shell, CONNECT_ERROR_CODE,
											"See Details", targetID.getName(),
											exception).open();
								}
							});
						}
						return new Status(IStatus.OK, Activator.PLUGIN_ID, 0,
								"", null);
					}

				}).run(null);

		return true;
	}
}
