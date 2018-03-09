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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.TimeoutException;

public class AsyncReturnUtil {

	public static boolean isAsyncType(Class<?> type) {
		return (type == null) ? false
				: (Future.class.isAssignableFrom(type) || IFuture.class.isAssignableFrom(type));
	}

	@SuppressWarnings("rawtypes")
	public static Object asyncReturn(Object returnObject, Class<?> asyncReturnType, long timeout) throws TimeoutException,
			InterruptedException, ExecutionException, java.util.concurrent.TimeoutException, InvocationTargetException {
		if (returnObject == null)
			return null;
		else if (asyncReturnType.isAssignableFrom(IFuture.class))
			return ((IFuture) returnObject).get();
		else if (asyncReturnType.isAssignableFrom(Future.class))
			return ((Future) returnObject).get(timeout,TimeUnit.MILLISECONDS);
		return returnObject;
	}
}
