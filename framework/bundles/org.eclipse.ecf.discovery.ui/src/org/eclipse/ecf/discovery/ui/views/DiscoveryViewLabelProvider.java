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

package org.eclipse.ecf.discovery.ui.views;

import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

class DiscoveryViewLabelProvider extends LabelProvider {

	DiscoveryViewLabelProvider() {
		// nothing to do
	}

	public String getText(Object obj) {
		if (obj instanceof DiscoveryViewTreeParent) {
			final DiscoveryViewTreeParent tp = (DiscoveryViewTreeParent) obj;
			final IServiceID svcID = tp.getID();
			if (svcID == null)
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
		String imageKey = null;
		if (obj instanceof DiscoveryViewTreeParent) {
			if (((DiscoveryViewTreeParent) obj).getID() != null) {
				imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			} else {
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			}
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}