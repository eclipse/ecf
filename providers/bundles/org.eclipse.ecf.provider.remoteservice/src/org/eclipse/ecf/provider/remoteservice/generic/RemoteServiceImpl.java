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
package org.eclipse.ecf.provider.remoteservice.generic;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;

public class RemoteServiceImpl extends AbstractRemoteService {

	protected static final long DEFAULT_TIMEOUT = new Long(System.getProperty("ecf.remotecall.timeout", "30000")).longValue(); //$NON-NLS-1$ //$NON-NLS-2$

	protected RemoteServiceRegistrationImpl registration = null;
	protected RegistrySharedObject sharedObject = null;

	public RemoteServiceImpl(RegistrySharedObject sharedObject, RemoteServiceRegistrationImpl registration) {
		this.sharedObject = sharedObject;
		this.registration = registration;
	}

	protected IRemoteServiceID getRemoteServiceID() {
		return registration.getID();
	}

	protected IRemoteServiceReference getRemoteServiceReference() {
		return registration.getReference();
	}

	protected String[] getInterfaceClassNames() {
		return registration.getClasses();
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.remoteservice.IRemoteService#callAsync(org.eclipse.ecf.remoteservice.IRemoteCall, org.eclipse.ecf.remoteservice.IRemoteCallListener)
	 */
	public void callAsync(final IRemoteCall call, final IRemoteCallListener listener) {
		callAsyncWithTimeout(call, new Callable<IRemoteCallCompleteEvent>() {
			public IRemoteCallCompleteEvent call() throws Exception {
				final AtomicReference<IRemoteCallCompleteEvent> ar = new AtomicReference<IRemoteCallCompleteEvent>();
				sharedObject.sendCallRequestWithListener(registration, call, new IRemoteCallListener() {
					public void handleEvent(IRemoteCallEvent event) {
						if (event instanceof IRemoteCallCompleteEvent)
							synchronized (ar) {
								ar.set((IRemoteCallCompleteEvent) event);
								ar.notify();
							}
					}
				});
				synchronized (ar) {
					while (true) {
						IRemoteCallCompleteEvent result = ar.get();
						if (result != null)
							return result;
						ar.wait(call.getTimeout());
					}
				}
			}
		}, listener);
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.remoteservice.IRemoteService#callSync(org.eclipse.ecf.remoteservice.IRemoteCall)
	 */
	public Object callSync(IRemoteCall call) throws ECFException {
		return sharedObject.callSynch(registration, call);
	}

	/**
	 * @since 3.0
	 * @see org.eclipse.ecf.remoteservice.IRemoteService#fireAsync(org.eclipse.ecf.remoteservice.IRemoteCall)
	 */
	public void fireAsync(IRemoteCall call) throws ECFException {
		sharedObject.sendFireRequest(registration, call);
	}

}
