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

/**
 *
 */
public class DiscoveryViewTypeTreeObject extends ViewTreeObject {

	private final ArrayList children;

	/**
	 * @param name
	 */
	public DiscoveryViewTypeTreeObject(String name) {
		super(name);
		children = new ArrayList();
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
