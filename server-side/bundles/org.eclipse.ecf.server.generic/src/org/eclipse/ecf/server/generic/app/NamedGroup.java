/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.server.generic.app;

public class NamedGroup {
	Connector parent;
	String name;

	public NamedGroup(String name) {
		this.name = name;
	}

	protected void setParent(Connector c) {
		this.parent = c;
	}

	public Connector getConnector() {
		return parent;
	}

	public String getRawName() {
		return name;
	}

	public String getName() {
		return cleanGroupName(name);
	}

	public String getIDForGroup() {
		return parent.getID() + getName();
	}

	protected String cleanGroupName(String n) {
		String res = ((n.startsWith("/")) ? n : "/" + n); //$NON-NLS-1$ //$NON-NLS-2$
		return res;
	}
}