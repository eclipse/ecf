/****************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.asyncproxy;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ImmediateExecutor;

public class AsyncReturnUtil {

	public static boolean isAsyncType(final Class<?> type) {
		return (type == null) ? false
				: (Future.class.isAssignableFrom(type) || IFuture.class.isAssignableFrom(type));
	}

	public static boolean isAsyncType(String className) {
		return (className == null) ? false
				: (Future.class.getName().equals(className)
						|| IFuture.class.getName().equals(className));
	}
	
	public static Object convertAsyncToReturn(final Object returnObject, final Class<?> asyncReturnType, long timeout)
			throws InterruptedException, ExecutionException, java.util.concurrent.TimeoutException,
			InvocationTargetException {
		if (returnObject == null)
			return null;
		else if (asyncReturnType.isAssignableFrom(Future.class))
			return ((Future<?>) returnObject).get(timeout, TimeUnit.MILLISECONDS);
		else if (asyncReturnType.isAssignableFrom(IFuture.class))
			return ((IFuture<?>) returnObject).get();
		return null;
	}

	private static IFuture<?> createIFuture(final Object returnObject) {
		return new ImmediateExecutor().execute(new IProgressRunnable<Object>() {
			public Object run(IProgressMonitor monitor) throws Exception {
				return returnObject;
			}
		}, null);
	}
	
	private static Future<?> createFuture(final Object returnObject) {
		 return Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
			public Object call() throws Exception {
				return returnObject;
			}});
	}
	
	public static Object convertReturnToAsync(final Object returnObject, final Class<?> returnType) {
		if (IFuture.class.isAssignableFrom(returnType)) 
			return createIFuture(returnObject);
		else if (Future.class.isAssignableFrom(returnType)) 
			return createFuture(returnObject);
		return null;
	}

	public static Object convertReturnToAsync(final Object returnObject, final String returnType) {
		if (IFuture.class.getName().equals(returnType)) {
			return createIFuture(returnObject);
		} else if (Future.class.getName().equals(returnType)) {
			return createFuture(returnObject);
		}
		return null;
	}
}
