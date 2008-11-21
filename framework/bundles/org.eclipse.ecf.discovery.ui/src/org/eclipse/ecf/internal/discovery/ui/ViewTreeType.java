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

package org.eclipse.ecf.internal.discovery.ui;

import java.util.ArrayList;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

/**
 *
 */
public class ViewTreeType extends ViewTreeObject {

	private final ArrayList children;

	private final IServiceTypeID typeID;

	public ViewTreeType(String name) {
		super(name);
		this.typeID = null;
		children = new ArrayList();
	}

	/**
	 * @param name
	 */
	public ViewTreeType(IServiceTypeID typeID) {
		super(null);
		this.typeID = typeID;
		children = new ArrayList();
	}

	// overridden to workaround bug 255481
	public String getName() {
		String superName = super.getName();
		if (superName != null) {
			return superName;
		}

		String[] services = typeID.getServices();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < services.length; i++) {
			buffer.append(services[i]).append(':');
		}
		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString();
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typeID == null) ? 0 : typeID.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewTreeType other = (ViewTreeType) obj;
		if (typeID == null) {
			if (other.typeID != null)
				return false;
		} else if (!typeID.equals(other.typeID))
			return false;
		return true;
	}

}
