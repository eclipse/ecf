package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractFuture implements IFuture, ISafeProgressRunner {

	public abstract boolean isCanceled();

	public abstract IProgressMonitor getProgressMonitor();

}
