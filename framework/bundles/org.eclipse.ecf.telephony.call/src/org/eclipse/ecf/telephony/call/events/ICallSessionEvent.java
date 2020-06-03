/****************************************************************************
 * Copyright (c) 2004 Composent, Inc..
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.telephony.call.events;

import org.eclipse.ecf.telephony.call.CallSessionState;
import org.eclipse.ecf.telephony.call.ICallSession;
import org.eclipse.ecf.telephony.call.ICallSessionListener;

/**
 * Event received upon changes to the {@link CallSessionState}
 * of {@link ICallSession} instances.
 */
public interface ICallSessionEvent {
	/**
	 * Get the underlying {@link ICallSession} that is
	 * responsible for this event.  Receivers of this event
	 * (via {@link ICallSessionListener}) can get the
	 * ICallSession and call methods on that ICallSession when
	 * received.
	 * 
	 * @return ICallSession of the underlying ICallSession.  Will not be <code>null</code>.
	 */
	public ICallSession getCallSession();
}
