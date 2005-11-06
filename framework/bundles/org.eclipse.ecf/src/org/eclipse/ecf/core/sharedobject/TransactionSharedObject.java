package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.ISharedObjectContainerTransaction;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.identity.ID;

public class TransactionSharedObject extends AbstractSharedObject {
	
	protected TwoPhaseCommitEventProcessor transaction = null;
	protected int timeout = -1;
	
	public TransactionSharedObject() {
		super();
	}
	public TransactionSharedObject(int timeout) {
		this();
		this.timeout = timeout;
	}
	protected void initialize() {
		transaction = new TwoPhaseCommitEventProcessor(this,timeout);
		addEventProcessor(transaction);
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
