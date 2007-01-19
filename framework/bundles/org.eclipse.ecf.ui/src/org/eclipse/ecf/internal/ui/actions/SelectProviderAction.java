/*******************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent, Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.ui.actions;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.internal.ui.wizards.IWizardRegistryConstants;
import org.eclipse.ecf.ui.IConfigurationWizard;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

public class SelectProviderAction implements IWizardRegistryConstants,
		IWorkbenchWindowActionDelegate, IWorkbenchWindowPulldownDelegate {

	private IWorkbenchWindow window;

	private Menu menu;

	private HashMap map = new HashMap();

	public SelectProviderAction() {
		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtension[] configurationWizards = registry.getExtensionPoint(
					CONFIGURE_EPOINT_ID).getExtensions();
			IExtension[] connectWizards = registry.getExtensionPoint(
					CONNECT_EPOINT_ID).getExtensions();
			for (int i = 0; i < connectWizards.length; i++) {
				final IConfigurationElement[] ices = connectWizards[i]
						.getConfigurationElements();
				for (int j = 0; j < ices.length; j++) {
					final String factoryName = ices[j]
							.getAttribute(ATT_CONTAINER_TYPE_NAME);
					final IConfigurationWizard wizard = getWizard(
							configurationWizards, factoryName);
					final IConfigurationElement ice = ices[j];
					if (wizard == null) {
						map.put(ice.getAttribute(ATT_NAME),
								new SelectionAdapter() {
									public void widgetSelected(SelectionEvent e) {
										openConnectWizard(ice, factoryName);
									}
								});
					} else {
						map.put(ice.getAttribute(ATT_NAME),
								new SelectionAdapter() {
									public void widgetSelected(SelectionEvent e) {
										openConnectWizard(wizard, ice,
												factoryName);
									}
								});
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openConnectWizard(IConfigurationElement element,
			String factoryName) {
		try {
			IContainer container = ContainerFactory.getDefault()
					.createContainer(factoryName);
			IConnectWizard icw = (IConnectWizard) element
					.createExecutableExtension(ATT_CLASS);
			icw.init(window.getWorkbench(), container);
			WizardDialog dialog = new WizardDialog(window.getShell(), icw);
			dialog.open();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void openConnectWizard(IConfigurationWizard wizard,
			IConfigurationElement element, String factoryName) {
		try {
			IWorkbench workbench = window.getWorkbench();
			wizard.init(workbench, ContainerFactory.getDefault()
					.getDescriptionByName(factoryName));
			WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
			if (dialog.open() == WizardDialog.OK) {
				IConnectWizard icw = (IConnectWizard) element
						.createExecutableExtension(ATT_CLASS);
				icw.init(workbench, wizard.getConfigurationResult()
						.getContainer());
				dialog = new WizardDialog(window.getShell(), icw);
				dialog.open();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void dispose() {
		// dispose of the menu
		if (menu != null && !menu.isDisposed()) {
			menu.dispose();
		}
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do
	}

	private static IConfigurationWizard getWizard(IExtension[] extensions,
			String containerFactoryName) throws Exception {
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				if (containerFactoryName.equals(elements[j]
						.getAttribute(ATT_CONTAINER_TYPE_NAME))) {
					return (IConfigurationWizard) elements[j]
							.createExecutableExtension(ATT_CLASS);
				}
			}
		}
		return null;
	}

	public Menu getMenu(Control parent) {
		if (menu == null) {
			menu = new Menu(parent);
			for (Iterator it = map.keySet().iterator(); it.hasNext();) {
				String name = (String) it.next();
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(name);
				item.addSelectionListener((SelectionListener) map.get(name));
			}
		}
		return menu;
	}

}
