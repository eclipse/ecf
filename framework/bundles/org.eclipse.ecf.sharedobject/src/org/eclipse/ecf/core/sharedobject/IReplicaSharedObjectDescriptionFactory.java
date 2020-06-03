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
package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.identity.ID;

/**
 * Replica shared object factory
 * 
 */
public interface IReplicaSharedObjectDescriptionFactory {
	/**
	 * Create new ReplicaSharedObjectDescription instance for delivery to remote
	 * container identified by containerID parameter. The containerID parameter
	 * ID provided must not be null
	 * 
	 * @param containerID
	 * @return ReplicaSharedObjectDescription. Must not return null, but rather
	 *         a valid ReplicaSharedObjectDescription instance
	 */
	public ReplicaSharedObjectDescription createDescriptionForContainer(
			ID containerID, ISharedObjectConfig config);
}
