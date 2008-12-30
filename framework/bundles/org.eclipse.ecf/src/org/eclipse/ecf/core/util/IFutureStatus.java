/*******************************************************************************
* Copyright (c) 2008 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A future status object represents the future outcome of some operation.
 * It allows clients to access information about whether the operation is
 * completed (#isDone()), along with method to access the status information
 * associated with the completed operation ({@link #getStatus()}), and 
 * the actual results of the operation if completed successfully (i.e. {@link #get()} and
 * {@link #get(long)}.
 * 
 * Clients may also access an associated IProgressMonitor via {@link #getProgressMonitor()},
 * and the returned progress monitor allows cancellation of the underlying operation via
 * {@link IProgressMonitor#setCanceled(boolean)}.
 * 
 * @see IStatus
 * 
 */
public interface IFutureStatus {

	/**
	 * Waits if necessary for the operation to complete, and then retrieves
	 * and returns result.
	 * 
	 * @return Object that is the result of the asynchronous operation
	 *         represented by this future status. This method will block until a
	 *         result is available or the computation completes with some
	 *         exception.
	 * @throws InterruptedException
	 *             if thread waiting for result is interrupted.
	 * @throws CanceledException
	 *             if the operation has been canceled via the progress monitor {@link #getProgressMonitor()}.
	 */
	Object get() throws InterruptedException, CanceledException;

	/**
	 * Waits if necessary for at most the given time for the operation to
	 * complete, and then retrieves its result.
	 * 
	 * @param waitTimeInMillis
	 *            the maximum time to wait in milliseconds for the operation to complete.
	 * @return the result of the operation.
	 * @throws InterruptedException
	 *             if the current thread was interrupted while waiting.
	 * @throws TimeoutException
	 *             if the given wait time is exceeded without getting result.
	 * @throws CanceledException
	 *             if the operation has been canceled via the progress monitor {@link #getProgressMonitor()}.
	 */
	Object get(long waitTimeInMillis) throws InterruptedException, TimeoutException, CanceledException;

	/**
	 * Return progress monitor for this future status.  Will not return <code>null</code>.
	 * @return IProgressMonitor the progress monitor associated with the operation.
	 */
	public IProgressMonitor getProgressMonitor();

	/**
	 * Get status for operation.  Will return <code>null</code> if the operation is not complete 
	 * ({@link #isDone()} returns false).  If {@link #isDone()} returns <code>true</code>, then
	 * will return a non-<code>null</code> IStatus instance with information about the status of the
	 * completed operation.
	 * @return IStatus the status of the operation.  Will return <code>null</code> if {@link #isDone()}
	 * returns <code>false</code>.
	 */
	public IStatus getStatus();

	/**
	 * Returns <tt>true</tt> if the operation has been completed.
	 * 
	 * Completion may be due to normal termination, an exception, or
	 * cancellation -- in all of these cases, this method will return
	 * <tt>true</tt>.
	 * 
	 * @return <tt>true</tt> if the operation has completed in some manner.
	 */
	boolean isDone();

}
