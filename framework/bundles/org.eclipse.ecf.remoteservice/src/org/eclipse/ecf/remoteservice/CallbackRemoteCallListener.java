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

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;

/**
 * @since 4.1
 */
public class CallbackRemoteCallListener implements IRemoteCallListener {

	private IAsyncCallback callback;

	public CallbackRemoteCallListener(IAsyncCallback callback) {
		Assert.isNotNull(callback);
		this.callback = callback;
	}

	@SuppressWarnings("unchecked")
	public void handleEvent(IRemoteCallEvent event) {
		if (event instanceof IRemoteCallCompleteEvent) {
			IRemoteCallCompleteEvent cce = (IRemoteCallCompleteEvent) event;
			if (cce.hadException()) {
				callback.onFailure(cce.getException());
			} else {
				callback.onSuccess(cce.getResponse());
			}
		}
	}

}
