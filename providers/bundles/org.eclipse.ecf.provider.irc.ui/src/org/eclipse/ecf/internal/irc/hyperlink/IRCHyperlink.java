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

package org.eclipse.ecf.internal.irc.hyperlink;

import java.net.URI;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.internal.irc.ui.wizards.IRCConnectWizard;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 *
 */
public class IRCHyperlink implements IHyperlink {

	private static final String ECF_IRC_CONTAINER_NAME = "ecf.irc.irclib";
	private URI fURI;
	private IRegion fRegion;

	/**
	 * Creates a new URL hyperlink.
	 *
	 * @param region
	 * @param urlString
	 */
	public IRCHyperlink(IRegion region, URI uri) {
		Assert.isNotNull(uri);
		Assert.isNotNull(region);

		fRegion= region;
		fURI= uri;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getHyperlinkRegion()
	 */
	public IRegion getHyperlinkRegion() {
		return fRegion;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#open()
	 */
	public void open() {
		openConnectWizard(fURI.toString());
	}

	private void openConnectWizard(String uri) {
		try {
			IContainer container = ContainerFactory.getDefault()
					.createContainer(ECF_IRC_CONTAINER_NAME);
			IConnectWizard icw = (IConnectWizard) new IRCConnectWizard(uri);
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			icw.init(workbench, container);
			WizardDialog dialog = new WizardDialog(window.getShell(), icw);
			dialog.open();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel() {
		return null;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IHyperlink#getHyperlinkText()
	 */
	public String getHyperlinkText() {
		return null;
	}
	
	/**
	 * Returns the URL string of this hyperlink.
	 * 
	 * @return the URL string
	 * @since 3.2
	 */
	public String getURLString() {
		return fURI.toString();
	}

}
