package org.eclipse.ecf.core.sharedobject;

public interface ITransactionSharedObjectConfiguration {
	public static final int DEFAULT_TIMEOUT = 30000;
	public static final int DEFAULT_MIN_FAILED_TO_ABORT = 0;
	int getTimeout();
	int getMinFailedToAbort();
}