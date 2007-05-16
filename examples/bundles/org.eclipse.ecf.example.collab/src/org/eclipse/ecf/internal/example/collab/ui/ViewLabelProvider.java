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

package org.eclipse.ecf.internal.example.collab.ui;

import org.eclipse.ecf.internal.example.collab.ClientPlugin;
import org.eclipse.ecf.internal.example.collab.ClientPluginConstants;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


class ViewLabelProvider extends LabelProvider {
	public Image getImage(Object obj) {
		Image image = null;		//By default, no image exists for sharedObject, but if found to be a specific instance, load from plugin repository.
		ImageRegistry registry = ClientPlugin.getDefault().getImageRegistry();
		
		if (obj instanceof TreeUser) {
			image = registry.get(ClientPluginConstants.DECORATION_USER);
		} else if (obj instanceof TreeParent && (((TreeParent)obj).getTreeItem() != null)) {
			
			String childName = ((TreeParent)obj).getTreeItem().getLabel();
			//TODO: Come up with a better strategy of matching node type to entity decoration.
			if (childName.equals("Project")) {
				image = registry.get(ClientPluginConstants.DECORATION_PROJECT);
			} else if (childName.equals("Join Time")) {
				image = registry.get(ClientPluginConstants.DECORATION_TIME);
			} else if (childName.equals("Container Type")) {
				image = registry.get(ClientPluginConstants.DECORATION_TASK);
			}
		}
		
		return image;
		/*
		 * String imageKey = ISharedImages.IMG_OBJ_ELEMENT; if (sharedObject
		 * instanceof RosterParent) imageKey = ISharedImages.IMG_OBJ_PROJECT;
		 * return
		 * PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		 */
	}

	public String getText(Object obj) {
		return obj.toString();
	}
}