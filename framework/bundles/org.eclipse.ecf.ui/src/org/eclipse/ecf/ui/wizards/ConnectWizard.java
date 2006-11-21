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

package org.eclipse.ecf.ui.wizards;

import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.IImageFiles;
import org.eclipse.ecf.internal.ui.Messages;
import org.eclipse.ecf.ui.ContainerHolder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ConnectWizard extends Wizard {

	private IWorkbench workbench;

	private IStructuredSelection selection;

	protected ContainerHolder containerHolder;

	public ConnectWizard(ContainerHolder containerHolder) {
		this.containerHolder = containerHolder;
	}
	public boolean performFinish() {
		return false;
	}

	public void addPages() {
		setForcePreviousAndNextButtons(true);
		//addPage(connectWizardPage);
	}

	/**
	 * Initializes the wizard.
	 * 
	 * @param aWorkbench
	 *            the workbench
	 * @param currentSelection
	 *            the current selection
	 */
	public void init(IWorkbench aWorkbench,
			IStructuredSelection currentSelection) {
		this.workbench = aWorkbench;
		this.selection = currentSelection;

		ImageDescriptor wizardBannerImage = AbstractUIPlugin
		.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
				IImageFiles.CONNECT_WIZARD);
		
		if (wizardBannerImage != null)	setDefaultPageImageDescriptor(wizardBannerImage);

		setWindowTitle(Messages.ConnectWizard_title);
		setNeedsProgressMonitor(true);
	}

}
