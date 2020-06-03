/****************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice;

/**
 * Asynchronous callback contract.
 * 
 * @since 4.1
 */
public interface IAsyncCallback<ResultType> {

	/**
	 * This method will be invoked by an arbitrary
	 * thread when an asynchronous remote service call
	 * is successfully completed.  Only this method 
	 * xor {@link #onFailure(Throwable)} will be called
	 * for a given remote call.
	 * 
	 * @param result the result of the remote call.  May be <code>null</code>.
	 */
	public void onSuccess(ResultType result);

	/**
	 * This method will be invoked by an arbitrary thread
	 * when an asynchronous remote service call fails.  Only
	 * this method xor {@link #onSuccess(Object)} will be called
	 * for a given remote call.
	 * @param exception any exception associated with the failure.  Will not be <code>null</code>.
	 */
	public void onFailure(Throwable exception);

}
