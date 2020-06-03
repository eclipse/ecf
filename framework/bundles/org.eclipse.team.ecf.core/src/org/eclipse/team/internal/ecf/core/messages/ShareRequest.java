/****************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.team.internal.ecf.core.messages;

import java.io.Serializable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;

public class ShareRequest implements Serializable {

	private static final long serialVersionUID = 1260108598712425345L;

	private final ID fromId;
	private final String[] paths;
	private final int[] types;

	public ShareRequest(ID fromId, IResource[] resources) {
		this.fromId = fromId;

		paths = new String[resources.length];
		types = new int[resources.length];

		for (int i = 0; i < resources.length; i++) {
			Assert.isNotNull(resources[i]);
			paths[i] = resources[i].getFullPath().toString();
			types[i] = resources[i].getType();
		}
	}

	public ID getFromId() {
		return fromId;
	}

	public String[] getPaths() {
		return paths;
	}

	public int[] getTypes() {
		return types;
	}

}
