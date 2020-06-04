/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.example.collab;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject;

public class ClientEntry {
	IContainer container;
	EclipseCollabSharedObject sharedObject;
	String containerType;
	boolean isDisposed = false;

	public ClientEntry(String type, IContainer cont) {
		this.containerType = type;
		this.container = cont;
	}

	public IContainer getContainer() {
		return container;
	}

	public String getContainerType() {
		return containerType;
	}

	public void setSharedObject(EclipseCollabSharedObject sharedObject) {
		this.sharedObject = sharedObject;
	}

	public EclipseCollabSharedObject getSharedObject() {
		return sharedObject;
	}

	public boolean isDisposed() {
		return isDisposed;
	}

	public void dispose() {
		isDisposed = true;
		if (sharedObject != null) {
			sharedObject.destroySelf();
			sharedObject = null;
		}
	}
}