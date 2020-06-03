/****************************************************************************
 * Copyright (c) 2007 Chris Aniszczyk and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Chris Aniszczyk <caniszczyk@gmail.com> - initial API and implementation
Binary file (standard input) matches
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CategoryPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public CategoryPreferencePage() {
		// nothing
	}

	public CategoryPreferencePage(String title) {
		super(title);
	}

	public CategoryPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		return null;
	}

	public void init(IWorkbench workbench) {
		// nothing
	}

}
