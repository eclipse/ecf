/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.ui.ContainerConfigurationResult;
import org.eclipse.ecf.ui.IConfigurationWizard;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IWorkbench;

public class ConfigurationWizardNode extends WizardNode {

	protected ContainerTypeDescription typeDescription;

	protected ContainerConfigurationResult containerHolder = null;

	public ConfigurationWizardNode(IWorkbench workbench, WizardPage wizardPage, WorkbenchWizardElement wizardElement, ContainerTypeDescription containerTypeDescription) {
		super(workbench, wizardPage, wizardElement);
		this.typeDescription = containerTypeDescription;
	}

	public IWizard createWizard() throws CoreException {
		IConfigurationWizard configWizard = ((IConfigurationWizard) getWizardElement().createWizardForNode());
		configWizard.init(getWorkbench(), typeDescription);
		return configWizard;
	}

	public ContainerConfigurationResult getConfigurationResult() {
		if (containerHolder != null)
			return containerHolder;
		return ((IConfigurationWizard) getWizard()).getConfigurationResult();
	}
}
