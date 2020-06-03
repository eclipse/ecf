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
package org.eclipse.ecf.ui.dialogs;

import org.eclipse.ecf.ui.ContainerConfigurationResult;
import org.eclipse.ecf.ui.wizards.ConfigurationWizardSelectionWizard;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

public class ConfigurationWizardDialog extends WizardDialog {

	ConfigurationWizardSelectionWizard wizard = null;

	public ConfigurationWizardDialog(Shell shell, IWorkbench workbench,
			IStructuredSelection selection) {
		super(shell, new ConfigurationWizardSelectionWizard());
		this.wizard = (ConfigurationWizardSelectionWizard) getWizard();
		wizard.init(workbench, selection);
	}

	public ConfigurationWizardDialog(Shell shell, IWorkbench workbench) {
		this(shell, workbench, null);
	}

	public ContainerConfigurationResult getResult() {
		return wizard.getContainerConfigurationResult();
	}
}
