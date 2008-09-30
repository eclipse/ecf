/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.Serializable;
import java.security.AccessControlException;
import org.eclipse.core.runtime.Assert;

public class AddRegistrationRequest implements Serializable {
	private static final long serialVersionUID = -2671778516104780091L;
	String service;
	String filter;
	AccessControlException acc;
	AddRegistrationRequest parent;

	private boolean done = false;

	public AddRegistrationRequest(String service, String filter, AddRegistrationRequest parent) {
		Assert.isNotNull(service);
		this.service = service;
		this.filter = filter;
		this.parent = parent;
	}

	public String getService() {
		return service;
	}

	public String getFilter() {
		return filter;
	}

	public Integer getId() {
		return new Integer(System.identityHashCode(this));
	}

	public void waitForResponse(long timeout) {
		long startTime = System.currentTimeMillis();
		long endTime = startTime + timeout;
		synchronized (this) {
			while (!done && (endTime >= System.currentTimeMillis())) {
				try {
					wait(timeout / 10);
				} catch (InterruptedException e) {
					// just return;
					return;
				}
			}
		}
	}

	public boolean isDone() {
		return done;
	}

	public AccessControlException getException() {
		return acc;
	}

	public void notifyResponse(AccessControlException exception) {
		this.acc = exception;
		synchronized (this) {
			done = true;
			if (parent != null) {
				parent.notifyResponse(exception);
			} else {
				synchronized (this) {
					this.notify();
				}
			}
		}
	}
}