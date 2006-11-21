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
package org.eclipse.ecf.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.internal.ui.wizards.IWizardRegistryConstants;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.ecf.ui.IContainerHolder;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;

public class ConnectWizardDialog extends WizardDialog {

	public ConnectWizardDialog(Shell parentShell, IWorkbench workbench,
			IContainerHolder containerHolder) {
		super(parentShell, getWizard(workbench, containerHolder));
	}

	protected static IConnectWizard getWizard(IWorkbench workbench,
			IContainerHolder containerHolder) {
		IConnectWizard connectWizard = null;
		try {
			IConfigurationElement ce = findConnectWizardConfigurationElements(containerHolder)[0];
			connectWizard = (IConnectWizard) ce
					.createExecutableExtension(IWizardRegistryConstants.ATT_CLASS);
			connectWizard.init(workbench, containerHolder.getContainer());
		} catch (CoreException e) {
			// TODO show error dialog
			e.printStackTrace();
		} catch (Exception e) {
			// TODO show error dialog
		}
		return connectWizard;
	}

	public static IConfigurationElement[] findConnectWizardConfigurationElements(
			IContainerHolder containerHolder) {
		List result = new ArrayList();
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = reg
				.getExtensionPoint(IWizardRegistryConstants.CONNECT_EPOINT_ID);
		if (extensionPoint == null) {
			return null;
		}
		IConfigurationElement[] ce = extensionPoint.getConfigurationElements();
		for (int i = 0; i < ce.length; i++) {
			String value = ce[i]
					.getAttribute(IWizardRegistryConstants.ATT_CONTAINER_TYPE_NAME);
			if (value != null
					&& value.equals(containerHolder
							.getContainerTypeDescription().getName()))
				result.add(ce[i]);
		}
		return (IConfigurationElement[]) result
				.toArray(new IConfigurationElement[] {});
	}

	public boolean hasConnectWizard(IContainerHolder containerHolder) {
		return (findConnectWizardConfigurationElements(containerHolder).length > 0);
	}
}
