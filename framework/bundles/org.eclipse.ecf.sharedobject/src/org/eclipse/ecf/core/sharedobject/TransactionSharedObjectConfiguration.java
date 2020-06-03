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
 * Configuration parameters for transaction shared object creation and
 * replication.
 * 
 */
public class TransactionSharedObjectConfiguration implements
		ISharedObjectTransactionConfig {

	protected int timeout = DEFAULT_TIMEOUT;

	protected ISharedObjectTransactionParticipantsFilter participantsFilter = null;

	public TransactionSharedObjectConfiguration() {
		super();
	}

	public TransactionSharedObjectConfiguration(int timeout) {
		this(timeout, null);
	}

	public TransactionSharedObjectConfiguration(int timeout,
			ISharedObjectTransactionParticipantsFilter filter) {
		this.timeout = timeout;
		this.participantsFilter = filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.sharedobject.ITransactionConfiguration#getTimeout()
	 */
	public int getTimeout() {
		return timeout;
	}

	public ISharedObjectTransactionParticipantsFilter getParticipantsFilter() {
		return participantsFilter;
	}
}
