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
package org.eclipse.ecf.examples.remoteservices.hello;

import java.util.concurrent.Future;

import org.eclipse.ecf.remoteservice.IAsyncCallback;

/**
 * @since 2.0
 */
public interface IHelloAsync {

	public void helloAsync(String from, IAsyncCallback<String> callback);
	/**
	 * @since 4.0
	 */
	public Future<String> helloAsync(String from);

	/**
	 * @since 3.0
	 */
	public void helloMessageAsync(HelloMessage message, IAsyncCallback<String> callback);
	/**
	 * @since 4.0
	 */
	public Future<String> helloMessageAsync(HelloMessage message);

}
