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

import org.eclipse.ecf.core.ISharedObjectContainerTransaction;
import org.eclipse.ecf.core.ISharedObjectTransactionConfig;

/**
 * Superclass that replicates itself transactionally.  
 *
 */
public class TransactionSharedObject extends AbstractSharedObject {
	
	protected ISharedObjectContainerTransaction transaction = null;
	protected ISharedObjectTransactionConfig configuration = null;
	
	public TransactionSharedObject() {
		super();
		configuration = new TransactionSharedObjectConfiguration();
	}
	public TransactionSharedObject(int timeout) {
		super();
		configuration = new TransactionSharedObjectConfiguration(timeout);
	}
	/**
	 * Construct instance.  The config parameter, if given, is used to 
	 * configure the transactional replication of instance (or subclass instances).
	 * If the config parameter is null, no transactional replication messaging will occur (i.e.
	 * replicas will be requested, but no waiting for acknowledgement will occur).
	 * 
	 * @param config
	 */
	public TransactionSharedObject(ISharedObjectTransactionConfig config) {
		super();
		configuration = config;
	}
	protected void initialize() {
		if (configuration != null) {
			TwoPhaseCommitEventProcessor trans = new TwoPhaseCommitEventProcessor(this,configuration);
			addEventProcessor(trans);
			transaction = trans;
		}
	}
	public Object getAdapter(Class clazz) {
		if (clazz.equals(ISharedObjectContainerTransaction.class)) {
			return transaction;
		}
		return null;
	}
}
