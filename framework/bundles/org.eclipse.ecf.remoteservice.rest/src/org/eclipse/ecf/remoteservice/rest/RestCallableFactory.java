/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import org.eclipse.ecf.remoteservice.rest.util.HttpGetRequestType;
import org.eclipse.ecf.remoteservice.rest.util.RestRequestType;

import java.lang.reflect.Method;
import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.remoteservice.*;

public class RestCallableFactory {

	public static final long DEFAULT_TIMEOUT = new Long(System.getProperty("ecf.remotecall.rest.timeout", "30000")).longValue(); //$NON-NLS-1$ //$NON-NLS-2$

	public static IRemoteCallable createRestCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters, RestRequestType requestType, long timeout) {
		return new RemoteCallable(method, resourcePath, defaultParameters, requestType, timeout);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters, RestRequestType requestType) {
		return createRestCallable(method, resourcePath, defaultParameters, requestType, DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath, IRemoteCallParameter[] defaultParameters) {
		return createRestCallable(method, resourcePath, defaultParameters, new HttpGetRequestType(), DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath) {
		return createRestCallable(method, resourcePath, null, new HttpGetRequestType(), DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createRestCallable(String method) {
		return createRestCallable(method, method, null, new HttpGetRequestType(), DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath, RestRequestType requestType, long timeout) {
		return createRestCallable(method, resourcePath, null, requestType, timeout);
	}

	public static IRemoteCallable createRestCallable(String method, String resourcePath, RestRequestType requestType) {
		return createRestCallable(method, resourcePath, null, requestType, DEFAULT_TIMEOUT);
	}

	public static IRemoteCallable[][] createCallablesFromClasses(Class[] cls, List callables) {
		Assert.isNotNull(cls);
		Assert.isTrue(cls.length > 0);
		// First create result list to hold IRestCallable[]...for each Class
		List results = new ArrayList();
		for (int i = 0; i < cls.length; i++) {
			Method[] methods = getMethodsForClass(cls[i]);
			IRemoteCallable[] methodCallables = getCallablesForMethods(methods, callables);
			if (methodCallables != null && methodCallables.length > 0)
				results.add(methodCallables);
		}
		return (IRemoteCallable[][]) results.toArray(new IRemoteCallable[][] {});
	}

	static IRemoteCallable[] getCallablesForMethods(Method[] methods, List callables) {
		Assert.isNotNull(methods);
		Assert.isTrue(methods.length > 0);
		List results = new ArrayList();
		for (int i = 0; i < methods.length; i++) {
			IRemoteCallable callable = findCallableForName(methods[i].getName(), callables);
			if (callable != null)
				results.add(callable);
		}
		return (IRemoteCallable[]) results.toArray(new IRemoteCallable[] {});
	}

	private static IRemoteCallable findCallableForName(String fqMethodName, List callables) {
		if (callables == null || callables.isEmpty())
			return null;
		for (Iterator i = callables.iterator(); i.hasNext();) {
			IRemoteCallable callable = (IRemoteCallable) i.next();
			if (callable != null && fqMethodName.equals(callable.getMethod()))
				return callable;
		}
		return null;
	}

	private static Method[] getMethodsForClass(Class class1) {
		Method[] results = null;
		try {
			results = class1.getDeclaredMethods();
		} catch (Exception e) {
			logException("Could not get declared methods for class=" + class1.getName(), e); //$NON-NLS-1$
			return null;
		}
		return results;
	}

	private static void logException(String message, Throwable e) {
		if (message != null)
			System.out.println(message);
		if (e != null)
			e.printStackTrace();
	}

	public static Class[] getClazzesFromStrings(String[] clazzes) throws IllegalArgumentException {
		List results = new ArrayList();
		for (int i = 0; i < clazzes.length; i++) {
			Class clazz = getClazzFromString(clazzes[i]);
			if (clazz != null)
				results.add(clazz);
		}
		return (Class[]) results.toArray(new Class[] {});
	}

	public static Class getClazzFromString(String className) throws IllegalArgumentException {
		Class result = null;
		try {
			result = Class.forName(className);
		} catch (Exception e) {
			String errorMsg = "ClassNotFoundException for class with name=" + className; //$NON-NLS-1$
			logException(errorMsg, e);
			throw new IllegalArgumentException(errorMsg);
		} catch (NoClassDefFoundError e) {
			String errorMsg = "NoClassDefFoundError for class with name=" + className; //$NON-NLS-1$
			logException(errorMsg, e);
			throw new IllegalArgumentException(errorMsg);
		}
		return result;
	}

}
