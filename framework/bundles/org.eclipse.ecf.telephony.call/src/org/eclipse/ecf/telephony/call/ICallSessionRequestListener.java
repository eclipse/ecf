/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.telephony.call;

import org.eclipse.ecf.telephony.call.events.ICallSessionRequestEvent;

/**
 * Listener for call session creation requests.
 * 
 */
public interface ICallSessionRequestListener {

	/**
	 * Handle the {@link ICallSessionRequestEvent} specifying an incoming call
	 * request event. Instances implementing this interface maybe provided to
	 * {@link ICallSessionContainerAdapter#addCallSessionRequestListener(ICallSessionRequestListener)}
	 * and will subsequently be notified of incoming call initiation requests.
	 * <p>
	 * </p>
	 * Note that this method may be called by an arbitrary thread (not
	 * necessarily the UI-thread), so implementers must be prepared for this.
	 * Implementers of this method also should not block.
	 * 
	 * 
	 * @param event
	 *            the event to process. Will not be <code>null</code>.
	 */
	public void handleCallSessionRequest(ICallSessionRequestEvent event);

}
