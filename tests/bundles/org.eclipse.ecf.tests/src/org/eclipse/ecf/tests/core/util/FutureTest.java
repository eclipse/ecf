/*******************************************************************************
* Copyright (c) 2008 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.core.util;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ecf.core.util.IExecutor;
import org.eclipse.ecf.core.util.IFuture;
import org.eclipse.ecf.core.util.IProgressRunnable;
import org.eclipse.ecf.core.util.JobsExecutor;
import org.eclipse.ecf.core.util.ThreadsExecutor;
import org.eclipse.ecf.core.util.TimeoutException;

public class FutureTest extends TestCase {

	public final static int ITERATIONS = 30;
	public final static int WAITTIME = 500;
	public final static int SHORTDURATION = 1000;
	public final static int LONGDURATION = 40000;
	
	public final static Integer result = new Integer(1);
	
	IProgressRunnable createProgressRunnable() {
		return new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Throwable {
				// This should/will be a long running operation
				monitor.beginTask("1", ITERATIONS);
				if (monitor.isCanceled()) return null;
				Object lock = new Object();
				for(int i=0; i < ITERATIONS; i++) {
					System.out.print(i+" ");
					monitor.worked(1);
					if (i == ITERATIONS -1) System.out.println();
					synchronized (lock) {
						lock.wait(WAITTIME);
					}
					if (monitor.isCanceled()) return null;
				}
				return result;
			}
		};
	}
	
	IProgressRunnable createProgressRunnableWithNPE() {
		return new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Throwable {
				throw new NullPointerException("Our Exception");
			}
		};
	}

	protected IFuture createAndStartFuture() {
		IExecutor executor = new ThreadsExecutor();
		return executor.execute(createProgressRunnable(), null);
	}

	protected IFuture createAndStartJobsFuture() {
		IExecutor executor = new JobsExecutor("testjobsexecutor");
		return executor.execute(createProgressRunnable(), null);
	}

	protected IFuture createAndStartFutureWithNPE() {
		IExecutor executor = new ThreadsExecutor();
		return executor.execute(createProgressRunnableWithNPE(), null);
	}

	protected IFuture createAndStartJobsFutureWithNPE() {
		IExecutor executor = new JobsExecutor("testjobsexecutor");
		return executor.execute(createProgressRunnableWithNPE(), null);
	}

	public void testJobsGet() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartJobsFuture();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = future.get();
		assertNotNull(result);
		assertEquals(new Integer(1), result);
	}
	
	public void testJobsGetOKStatus() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartJobsFuture();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = future.get();
		assertNotNull(result);
		assertEquals(new Integer(1), result);
		IStatus status = future.getStatus();
		assertNotNull(status);
		assertTrue(status.isOK());
	}

	public void testJobsGetExceptionStatus() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartJobsFutureWithNPE();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = future.get();
		assertNull(result);
		IStatus status = future.getStatus();
		assertNotNull(status);
		assertTrue(status.getSeverity()==IStatus.ERROR);
		Throwable t = status.getException();
		assertNotNull(t);
		assertTrue(t instanceof NullPointerException);
	}


	public void testJobsGetWithLongTimeout() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartJobsFuture();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = future.get(LONGDURATION);
		assertNotNull(result);
		assertEquals(new Integer(1), result);
	}

	public void testJobsGetWithShortTimeout() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartJobsFuture();
		// We're playing the role of the client...so we'll wait for a second or so and then call get
		try {
			future.get(SHORTDURATION);
			fail();
		} catch (TimeoutException e) {
			assertTrue(e.getDuration() == SHORTDURATION);
		}
	}

	public void testJobsCancel() throws Exception {
		// The API implementer would do this
		final IFuture future = createAndStartFuture();
		// We're playing the role of the client...so we'll wait for a second or so and then call get
		try {
			future.cancel();
			future.get();
			// The above get should result in canceled Exception
			fail();
		} catch (OperationCanceledException e) {
			// This is expected...
			IStatus status = future.getStatus();
			assertNotNull(status);
			assertTrue(status.getSeverity()==IStatus.CANCEL);
		}
	}

	public void testJobsGetWithMultipleClientThreads() throws Exception {
		// The API implementer would do this
		final IFuture future = createAndStartJobsFuture();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						Object o = future.get();
						//System.out.println("thread "+Thread.currentThread().getName()+" got result = "+o);
						assertEquals(new Integer(1),o);
					} catch (OperationCanceledException e) {
						e.printStackTrace();
						fail();
					} catch (InterruptedException e) {
						e.printStackTrace();
						fail();
					}
				}},""+i);
			t.start();
			t.join();
		}
	}
	
	public void testJobsTimeoutWithMultipleClientThreads() throws Exception {
		// The API implementer would do this
		final IFuture future = createAndStartJobsFuture();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						future.get(SHORTDURATION);
						fail();
					} catch (OperationCanceledException e) {
						e.printStackTrace();
						fail();
					} catch (TimeoutException e) {
						System.out.println("thread "+Thread.currentThread().getName()+" timed out");
						assertTrue(e.getDuration() == SHORTDURATION);
					} catch (InterruptedException e) {
						e.printStackTrace();
						fail();
					}
				}},""+i);
			t.start();
			t.join();
		}
	}

	public void testJobsCancelWithMultipleClientThreads() throws Exception {
		// The API implementer would do this
		final IFuture future = createAndStartJobsFuture();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						future.cancel();
						future.get();
						fail();
					} catch (OperationCanceledException e) {
						System.out.println("thread "+Thread.currentThread().getName()+" canceled");
					} catch (InterruptedException e) {
						e.printStackTrace();
						fail();
					}
				}},""+i);
			t.start();
		}
		Thread.sleep(3000);
	}

	public void testGet() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartFuture();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = future.get();
		assertNotNull(result);
		assertEquals(new Integer(1), result);
	}
	
	public void testGetOKStatus() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartFuture();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = future.get();
		assertNotNull(result);
		assertEquals(new Integer(1), result);
		IStatus status = future.getStatus();
		assertNotNull(status);
		assertTrue(status.isOK());
	}
	
	public void testGetExceptionStatus() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartFutureWithNPE();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = future.get();
		assertNull(result);
		IStatus status = future.getStatus();
		assertNotNull(status);
		assertTrue(status.getSeverity()==IStatus.ERROR);
		Throwable t = status.getException();
		assertNotNull(t);
		assertTrue(t instanceof NullPointerException);
	}

	public void testGetWithLongTimeout() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartFuture();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = future.get(LONGDURATION);
		assertNotNull(result);
		assertEquals(new Integer(1), result);
	}

	public void testGetWithShortTimeout() throws Exception {
		// The API implementer would do this
		IFuture future = createAndStartFuture();
		// We're playing the role of the client...so we'll wait for a second or so and then call get
		try {
			future.get(SHORTDURATION);
			fail();
		} catch (TimeoutException e) {
			assertTrue(e.getDuration() == SHORTDURATION);
		}
	}

	public void testCancel() throws Exception {
		// The API implementer would do this
		final IFuture future = createAndStartFuture();
		// We're playing the role of the client...so we'll wait for a second or so and then call get
		try {
			future.cancel();
			future.get();
			// The above get should result in canceled Exception
			fail();
		} catch (OperationCanceledException e) {
			IStatus status = future.getStatus();
			assertNotNull(status);
			assertTrue(status.getSeverity()==IStatus.CANCEL);
		}
	}

	public void testGetWithMultipleClientThreads() throws Exception {
		// The API implementer would do this
		final IFuture future = createAndStartFuture();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						Object o = future.get();
						//System.out.println("thread "+Thread.currentThread().getName()+" got result = "+o);
						assertEquals(new Integer(1),o);
					} catch (OperationCanceledException e) {
						e.printStackTrace();
						fail();
					} catch (InterruptedException e) {
						e.printStackTrace();
						fail();
					}
				}},""+i);
			t.start();
			t.join();
		}
	}
	
	public void testTimeoutWithMultipleClientThreads() throws Exception {
		// The API implementer would do this
		final IFuture future = createAndStartFuture();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						future.get(SHORTDURATION);
						fail();
					} catch (OperationCanceledException e) {
						e.printStackTrace();
						fail();
					} catch (TimeoutException e) {
						System.out.println("thread "+Thread.currentThread().getName()+" timed out");
						assertTrue(e.getDuration() == SHORTDURATION);
					} catch (InterruptedException e) {
						e.printStackTrace();
						fail();
					}
				}},""+i);
			t.start();
			t.join();
		}
	}

	public void testCancelWithMultipleClientThreads() throws Exception {
		// The API implementer would do this
		final IFuture future = createAndStartFuture();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						future.cancel();
						future.get();
						fail();
					} catch (OperationCanceledException e) {
						System.out.println("thread "+Thread.currentThread().getName()+" canceled");
					} catch (InterruptedException e) {
						e.printStackTrace();
						fail();
					}
				}},""+i);
			t.start();
		}
		Thread.sleep(3000);
	}

}
