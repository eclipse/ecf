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
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IWorkbench;

public class ConnectWizardNode extends WizardNode {

	protected IContainer containerToConnect;

	public ConnectWizardNode(IWorkbench workbench, WizardPage wizardPage,
			WorkbenchWizardElement wizardElement, IContainer container) {
		super(workbench, wizardPage, wizardElement);
		this.containerToConnect = container;
	}

	public IWizard createWizard() throws CoreException {
		IConnectWizard connectWizard = ((IConnectWizard) getWizardElement()
				.createWizardForNode());
		connectWizard.init(getWorkbench(), containerToConnect);
		return connectWizard;
	}

}
