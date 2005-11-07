package org.eclipse.ecf.core.sharedobject;

public class TransactionSharedObjectConfiguration {
	
	public static final int DEFAULT_TIMEOUT = 30000;
	public static final int DEFAULT_MIN_FAILED_TO_ABORT = 0;
	
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
	public int getTimeout() {
		return timeout;
	}
	public int getMinFailedToAbort() {
		return minFailedToAbort;
	}
}
