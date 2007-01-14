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
package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.Constants;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class RosterGroup extends RosterParent {
	public RosterGroup(String name, ID svcID) {
		super(name, svcID);
		if (name == null || name.equals(""))
			setName(RosterView.UNFILED_GROUP_NAME);
	}

	public int getActiveCount() {
		RosterObject[] childs = getChildren();
		int totCount = 0;
		for (int i = 0; i < childs.length; i++) {
			if (childs[i] instanceof RosterBuddy) {
				RosterBuddy tb = (RosterBuddy) childs[i];
				IPresence presence = tb.getPresence();
				if (presence != null
						&& presence.getType().equals(
								IPresence.Type.AVAILABLE))
					totCount++;
			}
		}
		return totCount;
	}

	public Image getImage() {
		ImageRegistry registry = Activator.getDefault().getImageRegistry();
		return registry.get(Constants.DECORATION_GROUP);
	}

	public int getTotalCount() {
		return getChildren().length;
	}

	public String getLabel() {
		return getName() + " (" + getActiveCount() + "/" + getTotalCount()
				+ ")";
	}
}