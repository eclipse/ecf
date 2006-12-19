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

package org.eclipse.ecf.presence.ui;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ui.IViewPart;

/**
 * Roster view that supports adding multiple containers
 * 
 */
public interface IMultiRosterViewPart extends IViewPart {

	/**
	 * Add container to the roster view.  The container provided should
	 * adapter to the IPresenceContainerAdapter.  If it does not, then
	 * false will be returned.
	 * 
	 * @param container The container provided should
	 * adapter to the IPresenceContainerAdapter.  If it does not, then
	 * false will be returned.
	 * 
	 * @return true if the given container can be added to this roster view. False if not.
	 */
	public boolean addContainer(IContainer container);
}
