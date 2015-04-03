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

	public final static String ID = "org.eclipse.ecf.internal.remoteservices.ui.remoteServicePerspective";

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
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
		layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);
	}

	private void defineLayout(IPageLayout layout) {
		// Editors are placed for free.
		String editorArea = layout.getEditorArea();

		// Top left.
		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.26f, editorArea); //$NON-NLS-1$
		topLeft.addView("org.eclipse.ui.navigator.ProjectExplorer"); //$NON-NLS-1$

		// Bottom left.
		IFolderLayout bottomLeft = layout.createFolder("bottomLeft", //$NON-NLS-1$
				IPageLayout.BOTTOM, 0.66f, "topLeft"); //$NON-NLS-1$
		bottomLeft.addView(IPageLayout.ID_PROP_SHEET);

		// Bottom right.
//		IFolderLayout bottomRight = layout.createFolder("bottomRight", //$NON-NLS-1$
//				IPageLayout.BOTTOM, 0.66f, editorArea);

		// Top right.
		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.80f, editorArea); //$NON-NLS-1$
		topRight.addView(IPageLayout.ID_OUTLINE);
	}
}
