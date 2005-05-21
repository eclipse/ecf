/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.security;

import java.io.Serializable;
import java.security.PermissionCollection;

import org.eclipse.ecf.core.identity.ID;

public interface IJoinPolicy extends IContainerPolicy {
	/**
	 * Check join request
	 * 
	 * @param fromID
	 *            the ID of the container making the join request
	 * @param targetID
	 *            the ID of the container responding to that join request
	 * @param targetGroup
	 *            the target name of the group that is being joined
	 * @param joinData
	 *            arbitrary data associated with the join request
	 * @return PermissionCollection a collection of permissions associated with
	 *         a successful acceptance of join request
	 * @throws SecurityException
	 *             if join is to be refused
	 */
	public PermissionCollection checkJoin(ID fromID, ID targetID,
			String targetGroup, Serializable joinData) throws SecurityException;
}
