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
package org.eclipse.ecf.core.sharedobject;

/**
 * Configuration parameters for transaction shared object creation and replication. 
 *
 */
public class TransactionSharedObjectConfiguration implements ITransactionConfiguration {
	
	protected int timeout = DEFAULT_TIMEOUT;
	protected ITransactionParticipantsFilter participantsFilter = null;
	
	public TransactionSharedObjectConfiguration() {
		super();
	}
	public TransactionSharedObjectConfiguration(int timeout) {
		this(timeout,null);
	}
	public TransactionSharedObjectConfiguration(int timeout, ITransactionParticipantsFilter filter) {
		this.timeout = timeout;
		this.participantsFilter = filter;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.sharedobject.ITransactionConfiguration#getTimeout()
	 */
	public int getTimeout() {
		return timeout;
	}
	public ITransactionParticipantsFilter getParticipantsFilter() {
		return participantsFilter;
	}
}
