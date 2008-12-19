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

public interface IFutureStatus extends IStatus {

	/**
	 * State representing that the computation is still being done.
	 */
	public static final int IN_PROGRESS = 0x10;

	/**
	 * Return progress monitor for this future status.  Will not
	 * return <code>null</code>.
	 *
	 */
	public IProgressMonitor getProgressMonitor();

	/**
	 * Returns <tt>true</tt> if this task completed.
	 * 
	 * Completion may be due to normal termination, an exception, or
	 * cancellation -- in all of these cases, this method will return
	 * <tt>true</tt>.
	 * 
	 * @return <tt>true</tt> if this task completed
	 */
	boolean isDone();

	/**
	 * Waits if necessary for the computation to complete, and then retrieves
	 * its result.
	 * 
	 * @return Object that is the result of the asynchronous computation
	 *         represented by this future status. This method will block until a
	 *         result is available or the computation completes with some
	 *         exception.
	 * @throws InterruptedException
	 *             if thread waiting for result is interrupted.
	 * @throws CanceledException
	 *             if the operation has been cancelled via the progress monitor available via {@link #getProgressMonitor()}
	 */
	Object get() throws InterruptedException, CanceledException;

	/**
	 * Waits if necessary for at most the given time for the computation to
	 * complete, and then retrieves its result, if available.
	 * 
	 * @param waitTimeInMillis
	 *            the maximum time to wait in milliseconds
	 * @return the computed result
	 * @throws InterruptedException
	 *             if the current thread was interrupted while waiting.
	 * @throws TimeoutException
	 *             if the given wait time is exceeded without getting result.
	 * @throws CanceledException
	 *             if the operation has been cancelled via the progress monitor available via {@link #getProgressMonitor()}
	 */
	Object get(long waitTimeInMillis) throws InterruptedException, TimeoutException, CanceledException;

}
