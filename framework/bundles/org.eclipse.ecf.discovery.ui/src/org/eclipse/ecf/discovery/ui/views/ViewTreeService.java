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

import java.util.ArrayList;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;

class ViewTreeService extends ViewTreeObject {
	private final ArrayList children;

	private final IServiceID id;

	private final IServiceInfo serviceInfo;

	public ViewTreeService(IServiceID id, String name, IServiceInfo svcInfo) {
		super(name);
		this.id = id;
		children = new ArrayList();
		serviceInfo = svcInfo;
	}

	public IServiceInfo getServiceInfo() {
		return serviceInfo;
	}

	public IServiceID getID() {
		return id;
	}

	public void addChild(ViewTreeObject child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(ViewTreeObject child) {
		children.remove(child);
		child.setParent(null);
	}

	public ViewTreeObject[] getChildren() {
		return (ViewTreeObject[]) children.toArray(new ViewTreeObject[children.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public void clearChildren() {
		children.clear();
	}

}