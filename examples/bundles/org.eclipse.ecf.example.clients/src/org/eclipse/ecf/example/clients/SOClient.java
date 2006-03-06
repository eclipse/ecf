/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.example.clients;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.identity.ID;

public class SOClient {

	IContainer container = null;
	ISharedObjectContainer socontainer = null;
	ID targetID = null;
	
	protected void setupContainer(String type) throws Exception {
		container = ContainerFactory.getDefault().createContainer(type);
		socontainer = (ISharedObjectContainer) container.getAdapter(ISharedObjectContainer.class);
	}
}
