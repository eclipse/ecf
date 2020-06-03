/****************************************************************************
 * Copyright (c) 2015 Remain Software and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Willem Sietse Jongman - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.remoteservices.ui;

import java.util.List;

import org.eclipse.ecf.remoteserviceadmin.ui.endpoint.EndpointDiscoveryView;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.RemoteServiceAdminView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class RemoteServicePerspective implements IPerspectiveFactory {

	public final static String ID = "org.eclipse.ecf.remoteservices.ui.RemoteServicePerspective"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
		defineLayout(layout);
	}

	private void defineActions(IPageLayout layout) {
		// Add "new wizards".
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$

		// Add "show views".
		// to be replaced by IPageLayout.ID_PROJECT_EXPLORER
		layout.addShowViewShortcut("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$
		layout.addShowViewShortcut(EndpointDiscoveryView.ID_VIEW);
		layout.addShowViewShortcut(RemoteServiceAdminView.ID_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
	}

	private void defineLayout(IPageLayout layout) {
		// Editors are placed for free.
		String editorArea = layout.getEditorArea();
		// Folder for views at bottom
		IFolderLayout bottom = layout.createFolder("bottom", //$NON-NLS-1$
				IPageLayout.BOTTOM, 0.60f, editorArea);
		// Folder for views at left bottom
		IFolderLayout leftBottom = layout.createFolder("leftBottom", IPageLayout.LEFT, 0.30f, "bottom"); //$NON-NLS-1$ //$NON-NLS-2$
		// The ECF Endpoint Discovery view
		leftBottom.addView(EndpointDiscoveryView.ID_VIEW);
		// The ECF Service Discovery view
		leftBottom.addView("org.eclipse.ecf.discovery.ui.DiscoveryView"); //$NON-NLS-1$
		// Create folder for right bottom
		IFolderLayout rightBottom = layout.createFolder("rightBottom", //$NON-NLS-1$
				IPageLayout.RIGHT, 0.5f, "bottom"); //$NON-NLS-1$

		List<ServicesViewExtension> sves = Activator.getDefault().getLocalServicesViewExtensions();

		String viewId = "org.eclipse.pde.runtime.RegistryBrowser"; //$NON-NLS-1$
		if (sves.size() > 0) {
			viewId = sves.get(0).getViewId();
			Activator.getDefault().setLocalServicesViewId(viewId);
		}
		rightBottom.addView(viewId);
		// Add properties view in the middle
		bottom.addView(IPageLayout.ID_PROP_SHEET);
		// Create folder for left side above Endpoint Discover/Service Discovery
		// views
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.30f, editorArea); //$NON-NLS-1$
		left.addView(RemoteServiceAdminView.ID_VIEW);
		// Add error log view
		left.addView("org.eclipse.pde.runtime.LogView");
		// Top right.
		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.70f, editorArea); //$NON-NLS-1$
		// Add Project Explorer view
		topRight.addView("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$
		// Add Outline view
		topRight.addView(IPageLayout.ID_OUTLINE);

	}
}
