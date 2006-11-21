/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation, Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.wizards.ConfigurationWizardNode;
import org.eclipse.ecf.internal.ui.wizards.IWizardRegistryConstants;
import org.eclipse.ecf.internal.ui.wizards.WizardsRegistryReader;
import org.eclipse.ecf.internal.ui.wizards.WorkbenchWizardElement;
import org.eclipse.ecf.ui.ContainerHolder;
import org.eclipse.ecf.ui.IConfigurationWizard;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.wizards.IWizardCategory;

public class ConfigurationWizardSelectionPage extends
		AbstractWizardSelectionPage {

	private static final int CONTAINERTYPEDESCRIPTION_ERROR_CODE = 333;

	public ConfigurationWizardSelectionPage(IWorkbench workbench,
			IStructuredSelection selection) {
		super("createContainerWizardPage", workbench, selection);
	}

	protected ITriggerPoint getTriggerPoint() {
		return getWorkbench().getActivitySupport().getTriggerPointManager()
				.getTriggerPoint(IWizardRegistryConstants.CONFIGURE_EPOINT_ID);
	}

	protected ContainerHolder getContainerResult() {
		ConfigurationWizardNode cwn = (ConfigurationWizardNode) getSelectedNode();
		if (cwn == null)
			return null;
		return ((IConfigurationWizard) getSelectedNode().getWizard())
				.getConfigurationResult();
	}

	private ContainerTypeDescription getContainerTypeDescriptionForElement(
			WorkbenchWizardElement element) {
		ContainerTypeDescription typeDescription = ContainerFactory
				.getDefault().getDescriptionByName(
						element.getContainerTypeName());
		if (typeDescription == null) {
			String msg = "The container type name '" + element
					+ "' does not exist";
			setErrorMessage(msg);
			ErrorDialog.openError(getShell(), "Problem Opening Wizard",
					"The selected wizard could not be started.", new Status(
							IStatus.ERROR, Activator.PLUGIN_ID,
							CONTAINERTYPEDESCRIPTION_ERROR_CODE, msg, null));
			return null;
		}
		return typeDescription;
	}

	protected void updateSelectedNode(WorkbenchWizardElement wizardElement) {
		setErrorMessage(null);
		if (wizardElement == null) {
			updateMessage();
			setSelectedNode(null);
			return;
		}

		setSelectedNode(new ConfigurationWizardNode(getWorkbench(), this,
				wizardElement,
				getContainerTypeDescriptionForElement(wizardElement)));
		setMessage(wizardElement.getDescription());
	}

	protected IWizardCategory getRootCategory() {
		return new WizardsRegistryReader(Activator.PLUGIN_ID,
				IWizardRegistryConstants.CONFIGURE_EPOINT).getWizardElements();
	}

}
