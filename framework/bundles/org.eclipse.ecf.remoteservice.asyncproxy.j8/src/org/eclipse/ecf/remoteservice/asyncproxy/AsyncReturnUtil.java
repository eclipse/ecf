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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ImmediateExecutor;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;

public class AsyncReturnUtil {

	private static IFuture<?> createIFuture(final Object returnObject) {
		return new ImmediateExecutor().execute(new IProgressRunnable<Object>() {
			public Object run(IProgressMonitor monitor) throws Exception {
				return returnObject;
			}
		}, null);
	}
	
	private static Promise<?> createPromise(final Object returnObject) {
		Deferred<Object> deferred = new Deferred<Object>();
		deferred.resolve(returnObject);
		return deferred.getPromise();
	}
	
	private static CompletableFuture<?> createCompletableFuture(final Object returnObject) {
		CompletableFuture<Object> cf = new CompletableFuture<Object>();
		cf.complete(returnObject);
		return cf;
	}
	
	public static boolean isAsyncType(Class<?> type) {
		return (type == null) ? false
				: (CompletableFuture.class.isAssignableFrom(type) || CompletionStage.class.isAssignableFrom(type)
						|| Future.class.isAssignableFrom(type) || IFuture.class.isAssignableFrom(type)
						|| Promise.class.isAssignableFrom(type));
	}

	public static boolean isAsyncType(String className) {
		return (className == null) ? false
				: (CompletableFuture.class.getName().equals(className)
						|| CompletionStage.class.getName().equals(className) || Future.class.getName().equals(className)
						|| IFuture.class.getName().equals(className) || Promise.class.getName().equals(className));
	}
	
	public static Object convertAsyncToReturn(Object returnObject, Class<?> asyncReturnType, long timeout)
			throws InterruptedException, ExecutionException, java.util.concurrent.TimeoutException,
			InvocationTargetException {
		if (returnObject == null)
			return null;
		else if (asyncReturnType.isAssignableFrom(Future.class))
			return ((Future<?>) returnObject).get(timeout, TimeUnit.MILLISECONDS);
		else if (asyncReturnType.isAssignableFrom(CompletableFuture.class)) 
			return ((CompletableFuture<?>) returnObject).get(timeout, TimeUnit.MILLISECONDS);
		else if (asyncReturnType.isAssignableFrom(CompletionStage.class))
			return ((CompletionStage<?>) returnObject).toCompletableFuture().get(timeout, TimeUnit.MILLISECONDS);
		else if (asyncReturnType.isAssignableFrom(IFuture.class))
			return ((IFuture<?>) returnObject).get();
		else if (asyncReturnType.isAssignableFrom(Promise.class))
			return ((Promise<?>) returnObject).getValue();
		return null;
	}

	public static Object convertReturnToAsync(Object returnObject, Class<?> returnType) {
		if (IFuture.class.isAssignableFrom(returnType)) {
			return createIFuture(returnObject);
		} else if (Promise.class.isAssignableFrom(returnType)) {
			return createPromise(returnObject);
		} else if (CompletableFuture.class.isAssignableFrom(returnType)
				|| CompletionStage.class.isAssignableFrom(returnType) || Future.class.isAssignableFrom(returnType)) {
			return createCompletableFuture(returnObject);
		}
		return null;
	}

	public static Object convertReturnToAsync(Object returnObject, String returnType) {
		if (IFuture.class.getName().equals(returnType)) {
			return createIFuture(returnObject);
		} else if (Promise.class.getName().equals(returnType)) {
			return createPromise(returnObject);
		} else if (CompletableFuture.class.getName().equals(returnType)
				|| CompletionStage.class.getName().equals(returnType) || Future.class.getName().equals(returnType)) {
			return createCompletableFuture(returnObject);
		}
		return returnObject;
	}

}
