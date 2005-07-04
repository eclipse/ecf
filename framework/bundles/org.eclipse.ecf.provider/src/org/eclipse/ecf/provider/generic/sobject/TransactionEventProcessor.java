/**
 * 
 */
package org.eclipse.ecf.provider.generic.sobject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.ecf.core.ISharedObjectContainerTransaction;
import org.eclipse.ecf.core.SharedObjectAddAbortException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.provider.Trace;

/**
 * @author slewis
 *
 */
public class TransactionEventProcessor implements IEventProcessor {

	public static final Trace trace = Trace.create("transactioneventprocessor");
	public static final int DEFAULT_TIMEOUT = 30000;
	BaseSharedObject sharedObject = null;
	byte transactionState = ISharedObjectContainerTransaction.ACTIVE;
	Object lock = new Object();
	List participants = new Vector();
	Map failed = new HashMap();
	int timeout = DEFAULT_TIMEOUT;
	
	public TransactionEventProcessor(BaseSharedObject bse, int timeout) {
		sharedObject = bse;
		sharedObject.addEventProcessor(this);
		this.timeout = timeout;
	}
	public TransactionEventProcessor(BaseSharedObject bse) {
		this(bse,DEFAULT_TIMEOUT);
	}
    protected void trace(String msg) {
        if (Trace.ON && trace != null) {
            trace.msg(sharedObject.getID()+":"+msg);
        }
    }

    protected void traceDump(String msg, Throwable t) {
        if (Trace.ON && trace != null) {
            trace.dumpStack(t, sharedObject.getID()+":"+msg);
        }
    }

	protected void addParticipants(ID [] ids) {
        if (ids != null) {
            for(int i=0; i < ids.length; i++) {
            	trace("adding participant:"+ids[i]);
                if (!sharedObject.getHomeID().equals(ids[i])) participants.add(ids[i]);
            }
        }
	}
	protected void removeParticipant(ID id) {
		if (id != null) {
			trace("removing participant:"+id);
			participants.remove(id);
		}
	}
    protected void addFailed(ID remote, Throwable failure)
    {
        if (remote != null && failure != null) {
        	trace("adding failed:"+remote+":exception:"+failure);
            failed.put(remote, failure);
        }
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IEventProcessor#acceptEvent(org.eclipse.ecf.core.util.Event)
	 */
	public boolean acceptEvent(Event event) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.util.IEventProcessor#processEvent(org.eclipse.ecf.core.util.Event)
	 */
	public Event processEvent(Event e) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected byte getTransactionState() {
		synchronized (lock) {
			return transactionState;
		}
	}
	protected void waitToCommit() throws SharedObjectAddAbortException {
        synchronized (lock) {
            long end = System.currentTimeMillis() + timeout;
            try {
                while (!votingCompleted()) {
                    long wait = end - System.currentTimeMillis();
                    trace("waitForFinish waiting "+wait+"ms on "+sharedObject.getID());
                    if (wait <= 0L) throw new SharedObjectAddAbortException("Timeout waiting for create responses",(Throwable) null,timeout);
                    // Wait right here
                    lock.wait(wait);
                }
            } catch (InterruptedException e) {
                throw new SharedObjectAddAbortException("Wait interrupted",(Throwable) null,timeout);
            } catch (SharedObjectAddAbortException e1) {
                // Aborted for some reason.  Clean up.
                doAbort(e1);
            }
            // Success.  Send commit to remotes and clean up before returning.
            doCommit();
        }
	}
	protected void doAbort(Throwable t) {
		// XXX TODO
	}
	protected void doCommit() {
		// XXX TODO
	}
    protected boolean votingCompleted() throws SharedObjectAddAbortException
    {
        // The test here is is we've received any indication of failed participants in
        // the transaction.  If so, we throw.
    	trace("voting completed test");
        if (failed.size() > 0) {
            trace("voting completed. Failures:"+failed);
            // Abort!
            throw new SharedObjectAddAbortException("Abort received",failed,timeout);
            // If no problems, and the number of participants to here from is 0, then we're done
        } else if (getTransactionState() == ISharedObjectContainerTransaction.VOTING && participants.size() == 0) {
            // Success!
            trace("votingCompleted:true");
            return true;
        }
        // Else continue waiting
        trace("voting not completed");
        return false;
    }

}
