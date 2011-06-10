/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.Trace;

public class LogUtility {

	public static void logError(String methodName, String debugOption,
			Class clazz, String message) {
		logError(methodName, debugOption, clazz, message, null);
		traceException(methodName, debugOption, clazz, message, null);
	}
	
	public static void logInfo(String methodName, String debugOption,
			Class clazz, String message) {
		trace(methodName, debugOption, clazz, "INFO:" + message); //$NON-NLS-1$
		Activator.getDefault().log(
				new Status(IStatus.INFO, Activator.PLUGIN_ID,
						IStatus.INFO, clazz.getName() + ":" //$NON-NLS-1$
								+ ((methodName == null) ? "<unknown>" //$NON-NLS-1$
										: methodName) + ":" //$NON-NLS-1$
								+ ((message == null) ? "<empty>" : message), //$NON-NLS-1$
						null));
	}

	public static void logWarning(String methodName, String debugOption,
			Class clazz, String message) {
		trace(methodName, debugOption, clazz, "WARNING:" + message); //$NON-NLS-1$
		Activator.getDefault().log(
				new Status(IStatus.WARNING, Activator.PLUGIN_ID,
						IStatus.WARNING, clazz.getName() + ":" //$NON-NLS-1$
								+ ((methodName == null) ? "<unknown>" //$NON-NLS-1$
										: methodName) + ":" //$NON-NLS-1$
								+ ((message == null) ? "<empty>" : message), //$NON-NLS-1$
						null));
	}

	public static void logError(String methodName, String debugOption,
			Class clazz, String message, Throwable t) {
		if (t != null)
			traceException(methodName, debugOption, clazz, message, t);
		else
			trace(methodName, debugOption, clazz, message);
		Activator.getDefault().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
						clazz.getName() + ":" //$NON-NLS-1$
								+ ((methodName == null) ? "<unknown>" //$NON-NLS-1$
										: methodName) + ":" //$NON-NLS-1$
								+ ((message == null) ? "<empty>" //$NON-NLS-1$
										: message), t));
	}

	public static void logWarning(String methodName, String debugOption,
			Class clazz, String message, Throwable t) {
		if (t != null)
			traceException(methodName, debugOption, clazz, message, t);
		else
			trace(methodName, debugOption, clazz, message);
		Activator.getDefault().log(
				new Status(IStatus.WARNING, Activator.PLUGIN_ID,
						IStatus.WARNING, clazz.getName() + ":" //$NON-NLS-1$
								+ ((methodName == null) ? "<unknown>" //$NON-NLS-1$
										: methodName) + ":" //$NON-NLS-1$
								+ ((message == null) ? "<empty>" //$NON-NLS-1$
										: message), t));
	}

	public static void logError(String methodName, String debugOption,
			Class clazz, IStatus status) {
		Throwable t = status.getException();
		if (t != null)
			traceException(methodName, debugOption, clazz, status.getMessage(),
					t);
		else
			trace(methodName, debugOption, clazz, status.getMessage());
		Activator.getDefault().log(status);
	}

	public static void logWarning(String methodName, String debugOption,
			Class clazz, IStatus status) {
		logError(methodName, debugOption, clazz, status);
	}

	public static void trace(String methodName, String debugOptions,
			Class clazz, String message) {
		Trace.trace(Activator.PLUGIN_ID, debugOptions, clazz, methodName,
				message);
	}

	public static void traceException(String methodName, String debugOption,
			Class clazz, String message, Throwable t) {
		Trace.catching(Activator.PLUGIN_ID, debugOption, clazz,
				((methodName == null) ? "<unknown>" : methodName) + ":" //$NON-NLS-1$ //$NON-NLS-2$
						+ ((message == null) ? "<empty>" : message), t); //$NON-NLS-1$
	}

}
