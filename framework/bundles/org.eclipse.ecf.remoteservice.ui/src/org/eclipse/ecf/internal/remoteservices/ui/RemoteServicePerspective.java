/*******************************************************************************
 * Copyright (c) 2015 Remain Software and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Willem Sietse Jongman - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.remoteservices.ui;

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
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
	}

	private void defineLayout(IPageLayout layout) {
		// Editors are placed for free.
		String editorArea = layout.getEditorArea();

		// Top left - Project Explorer
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.33f, editorArea); //$NON-NLS-1$
		left.addView("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$

		// Bottom left - Endpoint Discovery
		IFolderLayout leftBottom = layout.createFolder("leftBottom", IPageLayout.BOTTOM, 0.60f, "left"); //$NON-NLS-1$ //$NON-NLS-2$
		leftBottom.addView(EndpointDiscoveryView.ID_VIEW);
		
		// Bottom middle - Properties
		IFolderLayout bottom = layout.createFolder("bottom", //$NON-NLS-1$
				IPageLayout.BOTTOM, 0.60f, editorArea);
		bottom.addView(IPageLayout.ID_PROP_SHEET);
		
		// Bottom right - RegistryBrowser
		IFolderLayout rightBottom = layout.createFolder("rightBottom", //$NON-NLS-1$
				IPageLayout.RIGHT, 0.50f, "bottom"); //$NON-NLS-1$
		rightBottom.addView("org.eclipse.pde.runtime.RegistryBrowser"); //$NON-NLS-1$

		// Top right.
		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.70f, editorArea); //$NON-NLS-1$
		topRight.addView(IPageLayout.ID_OUTLINE);
	}
}
