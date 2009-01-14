package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractFuture implements IFuture, ISafeProgressRunner {

	protected AbstractFuture() {
		// do thing.  Subclasses must override as appropriate
	}

	protected abstract boolean isCanceled();

	protected abstract IProgressMonitor getProgressMonitor();

}
