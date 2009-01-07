package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractFuture implements IFuture, ICancelable, ISafeProgressRunner {

	protected IProgressMonitor progressMonitor;

	protected AbstractFuture() {
		// Do nothing.  Note that subclasses must set 
		// progressMonitor to some non-null value
	}

	protected AbstractFuture(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	protected void setProgressMonitor(IProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

}
