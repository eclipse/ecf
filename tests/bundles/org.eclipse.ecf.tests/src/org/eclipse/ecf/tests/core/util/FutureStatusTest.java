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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.core.util.CanceledException;
import org.eclipse.ecf.core.util.FutureStatus;
import org.eclipse.ecf.core.util.IFutureStatus;
import org.eclipse.ecf.core.util.IFutureCallable;
import org.eclipse.ecf.core.util.TimeoutException;

import junit.framework.TestCase;

public class FutureStatusTest extends TestCase {

	public final static int ITERATIONS = 30;
	public final static int WAITTIME = 500;
	public final static int SHORTDURATION = 1000;
	public final static int LONGDURATION = 40000;
	
	public final static Integer result = new Integer(1);
	
	IFutureCallable createBasicCallable() {
		return new IFutureCallable() {
			public Object call(IProgressMonitor monitor) throws Throwable {
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
	
	protected IFutureStatus createAndRunFutureStatus(IFutureCallable callable, String message, IProgressMonitor progressMonitor) {
		FutureStatus futureStatus = new FutureStatus(message,progressMonitor);
		Runnable r = futureStatus.setter(callable);
		Thread t = new Thread(r);
		// This starts a thread to call callable...
		t.start();
		return futureStatus;
	}
	
	protected IFutureStatus createAndRunFutureStatus() {
		return createAndRunFutureStatus(createBasicCallable(),"ecf",null);
	}
	
	public void testGet() throws Exception {
		// The API implementer would do this
		IFutureStatus futureStatus = createAndRunFutureStatus();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = futureStatus.get();
		assertNotNull(result);
		assertEquals(new Integer(1), result);
	}
	
	public void testGetWithLongTimeout() throws Exception {
		// The API implementer would do this
		IFutureStatus futureStatus = createAndRunFutureStatus();
		// We're the client...so we'll wait for a second or so and then call get
		Object result = futureStatus.get(LONGDURATION);
		assertNotNull(result);
		assertEquals(new Integer(1), result);
	}

	public void testGetWithShortTimeout() throws Exception {
		// The API implementer would do this
		IFutureStatus futureStatus = createAndRunFutureStatus();
		// We're playing the role of the client...so we'll wait for a second or so and then call get
		try {
			futureStatus.get(SHORTDURATION);
			fail();
		} catch (TimeoutException e) {
			assertTrue(e.getDuration() == SHORTDURATION);
		}
	}

	public void testCancel() throws Exception {
		// The API implementer would do this
		final IFutureStatus futureStatus = createAndRunFutureStatus();
		// We're playing the role of the client...so we'll wait for a second or so and then call get
		try {
			IProgressMonitor pm = futureStatus.getProgressMonitor();
			pm.setCanceled(true);
			futureStatus.get();
			// The above get should result in canceled Exception
			fail();
		} catch (CanceledException e) {
			// This is expected...
		}
	}

	public void testGetWithMultipleClientThreads() throws Exception {
		// The API implementer would do this
		final IFutureStatus futureStatus = createAndRunFutureStatus();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						Object o = futureStatus.get();
						//System.out.println("thread "+Thread.currentThread().getName()+" got result = "+o);
						assertEquals(new Integer(1),o);
					} catch (CanceledException e) {
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
		final IFutureStatus futureStatus = createAndRunFutureStatus();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						futureStatus.get(SHORTDURATION);
						fail();
					} catch (CanceledException e) {
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
		final IFutureStatus futureStatus = createAndRunFutureStatus();
		// For this we will have 10 client threads all trying to get the result
		for(int i=0; i < 10; i++) {
			final Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						IProgressMonitor pm = futureStatus.getProgressMonitor();
						pm.setCanceled(true);
						futureStatus.get();
						fail();
					} catch (CanceledException e) {
						System.out.println("thread "+Thread.currentThread().getName()+" canceled");
					} catch (TimeoutException e) {
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

}
