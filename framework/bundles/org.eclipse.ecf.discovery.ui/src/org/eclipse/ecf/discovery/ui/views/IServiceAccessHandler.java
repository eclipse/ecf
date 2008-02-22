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

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.jface.action.IContributionItem;

/**
 * serviceAccessHandler extensions must implement this interface.
 */
public interface IServiceAccessHandler {

	/**
	 * Get the menu items to contribute for the given IServiceInfo.  Implementers should return
	 * a non-null array of IContributionItem instances (menus or menu items).  These will 
	 * be added to the context menu of the service entry identified by the given service info.
	 * 
	 * @param serviceInfo the IServiceInfo for the contributions.  Will not be <code>null</code>.
	 * @return IContributionItem [] any contribution to the context menu for the given service info.  If <code>null</code>,
	 * then no items will be added to the context menu.
	 */
	public IContributionItem[] getContributionsForService(IServiceInfo serviceInfo);

}
