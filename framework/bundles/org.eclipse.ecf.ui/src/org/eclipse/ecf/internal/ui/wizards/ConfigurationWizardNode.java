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

package org.eclipse.ecf.internal.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.ui.IConfigurationWizard;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IWorkbench;

public class ConfigurationWizardNode extends WizardNode {

	private static final int CONTAINER_CREATE_EXCEPTION_ERROR_CODE = 222;

	protected ContainerTypeDescription typeDescription;

	protected IContainer configurationResult = null;

	public ConfigurationWizardNode(IWorkbench workbench, WizardPage wizardPage,
			WorkbenchWizardElement wizardElement,
			ContainerTypeDescription containerTypeDescription) {
		super(workbench, wizardPage, wizardElement);
		this.typeDescription = containerTypeDescription;
	}

	public IWizard createWizard() throws CoreException {
		WorkbenchWizardElement wizardElement = getWizardElement();
		IWizard result = null;
		try {
			result = wizardElement.createWizardForNode();
		} catch (CoreException e) {
			// If there is no class associated with wizard then we'll create an
			// IContainer now
			try {
				configurationResult = ContainerFactory.getDefault()
						.createContainer(typeDescription, null);
			} catch (ContainerCreateException e2) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.PLUGIN_ID,
						CONTAINER_CREATE_EXCEPTION_ERROR_CODE,
						"Could not create container instance", e2));
			}
			return null;
		}
		IConfigurationWizard configWizard = (IConfigurationWizard) result;
		configWizard.init(getWorkbench(), typeDescription);
		return configWizard;
	}

	public IContainer getConfigurationResult() {
		if (configurationResult != null)
			return configurationResult;
		else
			return ((IConfigurationWizard) getWizard())
					.getConfigurationResult();
	}
}
