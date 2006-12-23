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
package org.eclipse.ecf.presence.roster;

import org.eclipse.core.runtime.Assert;

public class RosterItem implements IRosterItem {

	protected String name;

	protected Object parent;

	protected RosterItem() {
	}

	public RosterItem(Object parent, String name) {
		Assert.isNotNull(name);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Object getParent() {
		return parent;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

}
