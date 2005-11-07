package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.ISharedObjectContainerTransaction;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.identity.ID;

/**
 * Shared object superclass that replicates transactionally.  Provides a shared object that replicates itself via two phase commit
 * protocol.
 *
 */
public class TransactionSharedObject extends AbstractSharedObject {
	
	protected ISharedObjectContainerTransaction transaction = null;
	protected ITransactionSharedObjectConfiguration configuration = null;
	
	public TransactionSharedObject() {
		super();
		configuration = new TransactionSharedObjectConfiguration();
	}
	public TransactionSharedObject(int timeout) {
		super();
		configuration = new TransactionSharedObjectConfiguration(timeout);
	}
	public TransactionSharedObject(ITransactionSharedObjectConfiguration config) {
		super();
		configuration = config;
	}
	protected void initialize() {
		TwoPhaseCommitEventProcessor trans = new TwoPhaseCommitEventProcessor(this,configuration);
		addEventProcessor(trans);
		transaction = trans;
	}
    public SharedObjectDescription getReplicaDescription(ID receiver) {
    	return new SharedObjectDescription(getID(), getClass().getName(),
        		getConfig().getProperties());
    }
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
