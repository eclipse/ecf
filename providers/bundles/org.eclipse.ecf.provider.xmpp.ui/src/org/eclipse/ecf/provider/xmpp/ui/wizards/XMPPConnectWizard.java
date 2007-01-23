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
package org.eclipse.ecf.provider.xmpp.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.IExceptionHandler;
import org.eclipse.ecf.internal.provider.xmpp.ui.Activator;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.ecf.ui.actions.AsynchContainerConnectAction;
import org.eclipse.ecf.ui.dialogs.ContainerConnectErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

public class XMPPConnectWizard extends Wizard implements IConnectWizard {

	private static final int CONNECT_ERROR_CODE = 7777;

	XMPPConnectWizardPage page;

	private Shell shell;

	private IContainer container;

	private ID targetID;

	private IConnectContext connectContext;

	public void addPages() {
		page = new XMPPConnectWizardPage();
		addPage(page);
	}

	public void init(IWorkbench workbench, IContainer container) {
		shell = workbench.getActiveWorkbenchWindow().getShell();
		this.container = container;
	}

	public boolean performFinish() {
		connectContext = ConnectContextFactory
				.createPasswordConnectContext(page.getPassword());

		try {
			targetID = IDFactory.getDefault().createID(
					container.getConnectNamespace(), page.getConnectID());
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
									new ContainerConnectErrorDialog(shell,
											CONNECT_ERROR_CODE, "See Details",
											targetID.getName(), exception)
											.open();
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
