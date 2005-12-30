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
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.identity.ID;

/**
 * Superclass that replicates itself transactionally.  
 *
 */
public class TransactionSharedObject extends AbstractSharedObject {
	
	protected ISharedObjectContainerTransaction transaction = null;
	protected ITransactionConfiguration configuration = null;
	
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
	public TransactionSharedObject(ITransactionConfiguration config) {
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
	/**
	 * Get a SharedObjectDescription for a replica to be created on a given receiver.
	 * 
	 * @param receiver the receiver the SharedObjectDescription is for
	 * @return SharedObjectDescription to be associated with given receiver.  A non-null
	 * SharedObjectDescription <b>must</b> be returned.
	 */
    protected SharedObjectDescription getReplicaDescription(ID receiver) {
    	return new SharedObjectDescription(getID(), getClass().getName(),
        		getConfig().getProperties());
    }
    /**
     * Default implementation.  This method is called by the TwoPhaseCommit event processor to
     * determine the SharedObjectDescriptions associated with the given receivers.  Receivers
     * may be null (meaning that all in group are to be receivers), and if so then this method
     * should return a SharedObjectDescription [] of length 1 with a single SharedObjectDescription
     * that will be used for all receivers.  If receivers is non-null, then the SharedObjectDescription [] 
     * result must be of <b>same length</b> as the receivers array.  This method calls the
     * getReplicaDescription method to create a replica description for each receiver.  If this method returns
     * null, <b>null replication is done</b>.
     * 
     * @param receivers an ID[] of the intended receivers for the resulting SharedObjectDescriptions.  If null,
     * then the <b>entire current group</b> is assumed to be the target, and this method should return a
     * SharedObjectDescription array of length 1, with a single SharedObjectDescription for all target receivers.
     * 
     * @return SharedObjectDescription[] to determine replica descriptions for each receiver.  A null return
     * value indicates that no replicas are to be created.  If the returned array is not null, then it <b>must</b>
     * be of same length as the receivers parameter.
     * 
     */
	protected SharedObjectDescription[] getReplicaDescriptions(ID[] receivers) {
    	SharedObjectDescription [] descriptions = null;
    	if (receivers == null || receivers.length == 1) {
    		descriptions = new SharedObjectDescription[1];
    		descriptions[0] = getReplicaDescription((receivers==null)?null:receivers[0]);
    	} else {
    		descriptions = new SharedObjectDescription[receivers.length];
    		for(int i=0; i < receivers.length; i++) {
    			descriptions[i] = getReplicaDescription(receivers[i]);
    		}
    	}
    	return descriptions;
	}
	public Object getAdapter(Class clazz) {
		if (clazz.equals(ISharedObjectContainerTransaction.class)) {
			return transaction;
		}
		return null;
	}
}
