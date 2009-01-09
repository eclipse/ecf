package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractFuture implements IFuture, ISafeProgressRunner {

	protected AbstractFuture() {
		// Do nothing.  Note that subclasses must set 
		// progressMonitor to some non-null value
	}

	protected abstract boolean isCanceled();

	protected abstract IProgressMonitor getProgressMonitor();

}
