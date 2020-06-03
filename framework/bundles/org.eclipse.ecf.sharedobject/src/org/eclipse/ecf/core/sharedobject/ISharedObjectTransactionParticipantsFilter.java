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
package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.identity.ID;

/**
 * Filter for determining transaction participants
 * 
 * @see ISharedObjectContainerTransaction
 * 
 */
public interface ISharedObjectTransactionParticipantsFilter {
	/**
	 * Return ID[] of participants to participate in transacton.
	 * 
	 * @param currentGroup
	 *            the current set of container group members
	 * @return intended participants in transaction. If null is returned, all
	 *         group members will be included in transaction.
	 */
	ID[] filterParticipants(ID[] currentGroup);
}
