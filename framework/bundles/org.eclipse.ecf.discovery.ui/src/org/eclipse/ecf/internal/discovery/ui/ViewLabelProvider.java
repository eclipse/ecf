/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.discovery.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ViewLabelProvider extends LabelProvider {

	public ViewLabelProvider() {
		// nothing to do
	}

	public String getText(Object obj) {
		if (obj instanceof ViewTreeType) {
			final ViewTreeType tp = (ViewTreeType) obj;
			return cleanTypeName(tp.getName());
		}
		return super.getText(obj);
	}

	private String cleanTypeName(String inputName) {
		String res = inputName.trim();
		while (res.startsWith("_")) { //$NON-NLS-1$
			res = res.substring(1);
		}
		final int dotLoc = res.indexOf('.');
		if (dotLoc != -1) {
			res = res.substring(0, dotLoc);
		}
		return res;
	}

	public Image getImage(Object obj) {
		if (obj instanceof ViewTreeType) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		return org.eclipse.ecf.internal.discovery.ui.Activator.getDefault().getServiceImage();
	}
}