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
import org.eclipse.core.runtime.jobs.Job;

/**
 * <p>
 * Contract for the actual execution of {@link IProgressRunnable}s.  Instances of
 * this interface must be able to provide resources to eventually execute a given
 * {@link IProgressRunnable}, upon calling {@link #execute(IProgressRunnable, IProgressMonitor)}. 
 * </p>
 * <p>
 * Note that implementations may decide what/how to execute the given runnable (i.e.
 * via a {@link Thread}, or a {@link Job}, or a ThreadPool or some other asynchronous
 * invocation mechanism.  But the intended contract of {@link #execute(IProgressRunnable, IProgressMonitor)} is that
 * the {@link IProgressRunnable#run(IProgressMonitor)} method will be invoked by
 * this executor in a timely manner <b>without</b> blocking.
 * </p>
 * 
 * @see IProgressRunnable
 * @see IFuture
 * @see #execute(IProgressRunnable, IProgressMonitor)
 */
public interface IExecutor {

	/**
	 * Execute the given {@link IProgressRunnable} (i.e. call {@link IProgressRunnable#run(IProgressMonitor)}
	 * asynchronously (without blocking).  Will return a non-<code>null</code> instance of {@link IFuture} that allows
	 * clients to inspect the state of the asynchronous execution and retrieve any results via {@link IFuture#get()}
	 * or {@link IFuture#get(long)}.
	 *   
	 * @param runnable the {@link IProgressRunnable} to invoke.  Must not be <code>null</code>.
	 * @param monitor any {@link IProgressMonitor} to be passed to the runnable.  May be <code>null</code>.
	 * @return {@link IFuture} to allow for inspection of the state of the computation by clients,
	 * as well as access to any return values of {@link IProgressRunnable#run(IProgressMonitor)}.  Will not
	 * be <code>null</code>.
	 */
	IFuture execute(IProgressRunnable runnable, IProgressMonitor monitor);

}
