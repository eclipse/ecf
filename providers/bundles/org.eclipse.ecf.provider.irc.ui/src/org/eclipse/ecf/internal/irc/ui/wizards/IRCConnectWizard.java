/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *    Scott Lewis <slewis@composent.com>
 *****************************************************************************/
package org.eclipse.ecf.internal.irc.ui.wizards;

import java.net.URI;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.IExceptionHandler;
import org.eclipse.ecf.internal.irc.ui.Activator;
import org.eclipse.ecf.internal.irc.ui.IRCUI;
import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.ecf.ui.actions.AsynchContainerConnectAction;
import org.eclipse.ecf.ui.dialogs.ContainerConnectErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

public final class IRCConnectWizard extends Wizard implements IConnectWizard {

	private Shell shell;

	private IRCConnectWizardPage page;

	private IContainer container;

	private ID targetID;

	private IConnectContext connectContext;

	private String uriString = null;

	private IExceptionHandler exceptionHandler = new IExceptionHandler() {
		public IStatus handleException(final Throwable exception) {
			if (exception != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						new ContainerConnectErrorDialog(shell, IStatus.ERROR,
								"See Details", targetID.getName(), exception)
								.open();
					}
				});
			}
			return new Status(IStatus.OK, Activator.PLUGIN_ID, IStatus.OK,
					"Connected", null);
		}
	};

	public IRCConnectWizard() {
		super();
	}

	public IRCConnectWizard(String uri) {
		super();
		uriString = uri;
	}

	public void addPages() {
		page = new IRCConnectWizardPage(uriString);
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
			new URI(page.getConnectID());
			targetID = IDFactory.getDefault().createID(
					container.getConnectNamespace(), page.getConnectID());
		} catch (Exception e) {
			MessageDialog.openError(shell, "Connect Error", NLS.bind(
					"Invalid connect ID: {0}", page.getConnectID()));
			return false;
		}

		IChatRoomManager manager = (IChatRoomManager) this.container
				.getAdapter(IChatRoomManager.class);

		IRCUI ui = new IRCUI(this.container, manager, exceptionHandler);
		ui.showForTarget(targetID);
		// If it's not already connected, then we connect this new container
		if (!ui.isContainerConnected()) 
			new AsynchContainerConnectAction(this.container, this.targetID,
					this.connectContext, exceptionHandler).run(null);


		return true;
	}

}
