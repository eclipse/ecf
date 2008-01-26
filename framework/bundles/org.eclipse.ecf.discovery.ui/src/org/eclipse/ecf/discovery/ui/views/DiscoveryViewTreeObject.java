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

import org.eclipse.core.runtime.IAdaptable;

class DiscoveryViewTreeObject implements IAdaptable {
	private final String name;

	private DiscoveryViewTreeParent parent;

	public DiscoveryViewTreeObject(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setParent(DiscoveryViewTreeParent parent) {
		this.parent = parent;
	}

	public DiscoveryViewTreeParent getParent() {
		return parent;
	}

	public String toString() {
		return getName();
	}

	public Object getAdapter(Class key) {
		return null;
	}
}