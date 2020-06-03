/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.IStatus;

/**
 * Contract for general exception handler
 */
public interface IExceptionHandler {

	/** 
	 * Handle given exception 
	 * @param exception the exception to handle. If null, no exception occurred.
	 * @return IStatus any status to return as part of asynchronous job.  Should not be null.
	 */
	public IStatus handleException(Throwable exception);
}
