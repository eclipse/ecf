/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.examples.remoteservices.hello;

import org.eclipse.ecf.remoteservice.IAsyncCallback;
import org.eclipse.ecf.remoteservice.IAsyncRemoteServiceProxy;
import org.eclipse.equinox.concurrent.future.IFuture;

/**
 * @since 2.0
 */
public interface IHelloAsync extends IAsyncRemoteServiceProxy {

	public void helloAsync(String from, IAsyncCallback<String> callback);
	public IFuture helloAsync(String from);

	/**
	 * @since 3.0
	 */
	public void helloMessageAsync(HelloMessage message, IAsyncCallback<String> callback);
	/**
	 * @since 3.0
	 */
	public IFuture helloMessageAsync(HelloMessage message);

}
