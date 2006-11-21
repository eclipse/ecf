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

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.IImageFiles;
import org.eclipse.ecf.ui.IConfigurationWizard;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class ConfigurationWizardSelectWizard extends Wizard {

	private IWorkbench workbench;

	private IStructuredSelection selection;

	protected IConfigurationWizard configurationWizard;

	protected ConfigurationWizardSelectWizardPage createContainerWizardPage;

	protected IContainer containerResult;
	
	public boolean performFinish() {
		if (createContainerWizardPage != null) {
			this.containerResult = createContainerWizardPage.getContainerResult();
		}
		return false;
	}

	public IContainer getContainerResult() {
		return this.containerResult;
	}
	
	public void addPages() {
		setForcePreviousAndNextButtons(true);
		createContainerWizardPage = new ConfigurationWizardSelectWizardPage(
				this.workbench, this.selection);
		addPage(createContainerWizardPage);
	}

	/**
	 * Initializes the wizard.
	 * 
	 * @param aWorkbench
	 *            the workbench
	 * @param currentSelection
	 *            the current selectio
	 */
	public void init(IWorkbench aWorkbench,
			IStructuredSelection currentSelection) {
		this.workbench = aWorkbench;
		this.selection = currentSelection;

		setDefaultPageImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						IImageFiles.USER_AVAILABLE));

		setNeedsProgressMonitor(true);
	}

}
