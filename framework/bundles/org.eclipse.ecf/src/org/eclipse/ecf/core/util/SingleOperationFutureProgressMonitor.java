/**
 * 
 */
package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.IProgressMonitor;

class SingleOperationFutureProgressMonitor implements IProgressMonitor {

	/**
	 * 
	 */
	private final SingleOperationFuture singleOperationFuture;
	private final IProgressMonitor monitor;

	public SingleOperationFutureProgressMonitor(SingleOperationFuture singleOperationFuture, IProgressMonitor progressMonitor) {
		this.singleOperationFuture = singleOperationFuture;
		this.monitor = progressMonitor;
	}

	public void beginTask(String name, int totalWork) {
		monitor.beginTask(name, totalWork);
	}

	public void done() {
		monitor.done();
	}

	public void internalWorked(double work) {
		monitor.internalWorked(work);
	}

	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	public void setCanceled(boolean value) {
		monitor.setCanceled(value);
		// If this is intended to cancel
		// the operation, then we also call
		// SingleOperationFuture.this.setCanceled()
		if (value)
			this.singleOperationFuture.setCanceled();
	}

	public void setTaskName(String name) {
		monitor.setTaskName(name);
	}

	public void subTask(String name) {
		monitor.subTask(name);
	}

	public void worked(int work) {
		monitor.worked(work);
	}

}