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
package org.eclipse.team.internal.ecf.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

public final class RemoteSyncInfo extends SyncInfo {

	private final IResource local;
	private final IResourceVariant remote;

	public RemoteSyncInfo(IResource local, IResourceVariant remote, IResourceVariantComparator comparator) {
		super(local, null, remote, comparator);
		this.local = local;
		this.remote = remote;
	}

	protected int calculateKind() throws TeamException {
		if (remote == null && !local.exists()) {
			// this fails in super.calculateKind(), caused after a resource that
			// exists locally but not remotely has been synchronized (as in,
			// deleted locally), the key probably lies in the tree's
			// isSupervised(IResource) implementation
			return IN_SYNC;
		}

		int kind = super.calculateKind();
		switch (kind) {
			case ADDITION :
				kind |= (kind & ~DIRECTION_MASK) | INCOMING;
				break;
			case DELETION :
				kind |= (kind & ~DIRECTION_MASK) | OUTGOING;
				break;
		}
		return kind;
	}

}
