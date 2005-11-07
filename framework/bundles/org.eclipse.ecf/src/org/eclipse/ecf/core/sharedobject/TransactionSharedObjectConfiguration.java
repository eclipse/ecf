package org.eclipse.ecf.core.sharedobject;

public class TransactionSharedObjectConfiguration implements ITransactionSharedObjectConfiguration {
	
	protected int timeout = DEFAULT_TIMEOUT;
	protected int minFailedToAbort = DEFAULT_MIN_FAILED_TO_ABORT;
	
	public TransactionSharedObjectConfiguration() {
		super();
	}
	public TransactionSharedObjectConfiguration(int timeout) {
		this(timeout,DEFAULT_MIN_FAILED_TO_ABORT);
	}
	public TransactionSharedObjectConfiguration(int timeout, int minFailedToAbort) {
		this.timeout = timeout;
		this.minFailedToAbort = minFailedToAbort;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.sharedobject.ITransactionSharedObjectConfiguration#getTimeout()
	 */
	public int getTimeout() {
		return timeout;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.sharedobject.ITransactionSharedObjectConfiguration#getMinFailedToAbort()
	 */
	public int getMinFailedToAbort() {
		return minFailedToAbort;
	}
}
