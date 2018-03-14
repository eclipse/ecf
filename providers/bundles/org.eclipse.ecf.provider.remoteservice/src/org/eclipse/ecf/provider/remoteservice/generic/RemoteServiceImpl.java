/*******************************************************************************
  * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.util.concurrent.TimeoutException;
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
		getFutureExecutorService(call).submit(new Runnable() {
			public void run() {
				final AtomicReference<IRemoteCallEvent> l = new AtomicReference<IRemoteCallEvent>();
				sharedObject.sendCallRequestWithListener(registration, call, new IRemoteCallListener() {
					public void handleEvent(IRemoteCallEvent event) {
						if (event instanceof IRemoteCallCompleteEvent) {
							synchronized (l) {
								l.set(event);
								l.notify();
							}
						}
					}
				});
				long timeout = call.getTimeout();
				Exception exception = null;
				IRemoteCallEvent rce = null;
				long sysTimeout = System.currentTimeMillis() + timeout;
				synchronized (l) {
					try {
						while (rce == null && System.currentTimeMillis() < sysTimeout) {
							l.wait(timeout / 10);
							rce = l.get();
						}
					} catch (InterruptedException e) {
						exception = e;
					}
				}
				if (rce != null)
					listener.handleEvent(rce);
				else {
					if (exception == null)
						exception = new TimeoutException("remote call method=" + call.getMethod() + " timed out after " + timeout + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					final Exception except = exception;
					listener.handleEvent(new IRemoteCallCompleteEvent() {
						public long getRequestId() {
							return 0;
						}

						public Object getResponse() {
							return null;
						}

						public boolean hadException() {
							return true;
						}

						public Throwable getException() {
							return except;
						}
					});
				}
			}
		});
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
