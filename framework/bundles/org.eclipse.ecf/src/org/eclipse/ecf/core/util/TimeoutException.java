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

import org.eclipse.core.runtime.*;
import org.eclipse.ecf.internal.core.ECFPlugin;

/**
 * Timeout exception thrown when timeout occurs
 * 
 */
public class TimeoutException extends CoreException {

	private static final long serialVersionUID = -3198307514925924297L;
	public final long duration;

	public TimeoutException(IStatus status, long time) {
		super(status);
		duration = time;
	}

	public TimeoutException(long time, String message) {
		super(new Status(IStatus.ERROR, ECFPlugin.PLUGIN_ID, IStatus.ERROR, message, null));
		this.duration = time;
	}

	public long getDuration() {
		return duration;
	}
}