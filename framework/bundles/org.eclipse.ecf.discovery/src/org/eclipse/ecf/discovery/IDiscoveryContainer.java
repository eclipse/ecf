/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.discovery;

public interface IDiscoveryContainer {
	public void addServiceListener(IServiceListener listener);
	public void removeServiceListener(IServiceListener listener);
	public void addServiceTypeListener(IServiceTypeListener listener);
	public void removeServiceTypeListener(IServiceTypeListener listener);
	public void registerService(IServiceInfo serviceInfo);
}
