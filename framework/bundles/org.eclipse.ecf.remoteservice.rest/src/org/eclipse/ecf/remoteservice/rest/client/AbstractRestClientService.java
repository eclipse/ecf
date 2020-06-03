/****************************************************************************
 * Copyright (c) 2014 Composent, Inc. and others.
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
package org.eclipse.ecf.remoteservice.rest.client;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.remoteservice.rest.Activator;
import org.eclipse.ecf.internal.remoteservice.rest.DebugOptions;
import org.eclipse.ecf.remoteservice.client.*;
import org.eclipse.ecf.remoteservice.rest.RestException;

public abstract class AbstractRestClientService extends AbstractClientService {

	public AbstractRestClientService(AbstractClientContainer container, RemoteServiceClientRegistration registration) {
		super(container, registration);
	}

	protected void trace(String methodName, String message) {
		Trace.trace(Activator.PLUGIN_ID, DebugOptions.REST_CLIENT_SERVICE, getClass(), methodName, message);
	}

	protected void logException(String string, Throwable e) {
		Activator a = Activator.getDefault();
		if (a != null)
			a.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, string, e));
	}

	protected void logWarning(String string, Throwable e) {
		Activator a = Activator.getDefault();
		if (a != null)
			a.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, string));
	}

	protected void handleException(String message, Throwable e, int responseCode, byte[] responseBody) throws RestException {
		logException(message, e);
		throw new RestException(message, e, responseCode, responseBody);
	}

	protected void handleException(String message, Throwable e, int responseCode) throws RestException {
		handleException(message, e, responseCode, null);
	}

}
