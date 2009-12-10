/*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.discovery;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.Trace;

public class LogUtility {

	public static void logError(String method, String message, Class clazz,
			Throwable t) {
		Activator.getDefault().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR,
						method + ":" + message, t)); //$NON-NLS-1$
		String msg = method + ":" + message;
		if (t == null) {
			Trace.trace(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
					msg);
		} else
			Trace.catching(Activator.PLUGIN_ID,
					DebugOptions.EXCEPTIONS_CATCHING, clazz, msg, t); //$NON-NLS-1$
	}

	public static void logInfo(String method, String message, Class clazz,
			Throwable t) {
		Activator.getDefault().log(
				new Status(IStatus.INFO, Activator.PLUGIN_ID, IStatus.INFO,
						method + ":" + message, t)); //$NON-NLS-1$
		String msg = method + ":" + message;
		if (t == null) {
			Trace.trace(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING,
					msg);
		} else
			Trace.catching(Activator.PLUGIN_ID,
					DebugOptions.EXCEPTIONS_CATCHING, clazz, msg, t); //$NON-NLS-1$
	}

}
