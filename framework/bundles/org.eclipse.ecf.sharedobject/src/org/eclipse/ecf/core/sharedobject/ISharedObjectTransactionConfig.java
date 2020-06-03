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

/**
 * Transaction configuration information
 * 
 */
public interface ISharedObjectTransactionConfig {
	public static final int DEFAULT_TIMEOUT = 30000;

	/**
	 * Called by transaction implementation to specify transaction timeout
	 */
	int getTimeout();

	/**
	 * Called by transaction implementation to specify filter for determining
	 * transaction participants
	 * 
	 * @return {@link ISharedObjectTransactionParticipantsFilter}. If this
	 *         method returns a non-null instance, that instance's
	 *         {@link ISharedObjectTransactionParticipantsFilter#filterParticipants(org.eclipse.ecf.core.identity.ID[]) }
	 *         method will be called
	 */
	ISharedObjectTransactionParticipantsFilter getParticipantsFilter();
}