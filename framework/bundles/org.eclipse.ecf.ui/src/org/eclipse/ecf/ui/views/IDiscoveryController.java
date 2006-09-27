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

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.discovery.IDiscoveryContainer;
import org.eclipse.ecf.discovery.IServiceInfo;

public interface IDiscoveryController {
	public IDiscoveryContainer getDiscoveryContainer();
	public IContainer getContainer();
	public String [] getServiceTypes();
	public void connectToService(IServiceInfo service);
	public void startDiscovery();
	public boolean isDiscoveryStarted();
	public void stopDiscovery();
}
