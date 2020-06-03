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
 * Superclass for shared object classes that replicate themselves
 * transactionally.
 * 
 */
public class TransactionSharedObject extends BaseSharedObject {

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
	 * Construct instance. The config parameter, if given, is used to configure
	 * the transactional replication of instances or subclass instances. If the
	 * config parameter is null, no replication messaging will occur and only
	 * host instance of object will be created.
	 * 
	 * @param config
	 */
	public TransactionSharedObject(ISharedObjectTransactionConfig config) {
		super();
		configuration = config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.sharedobject.BaseSharedObject#initialize()
	 */
	protected void initialize() throws SharedObjectInitException {
		super.initialize();
		if (configuration != null) {
			TwoPhaseCommitEventProcessor trans = new TwoPhaseCommitEventProcessor(this, configuration);
			addEventProcessor(trans);
			transaction = trans;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.sharedobject.BaseSharedObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class clazz) {
		if (clazz.equals(ISharedObjectContainerTransaction.class)) {
			return transaction;
		}
		return super.getAdapter(clazz);
	}
}
