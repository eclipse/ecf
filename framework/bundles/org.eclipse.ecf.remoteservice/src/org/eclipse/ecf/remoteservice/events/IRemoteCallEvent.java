/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.remoteservice.events;

/**
 * Remote call event. Event received by service when a remote call should be
 * processed.
 */
public interface IRemoteCallEvent {

	/**
	 * Get request id for the given remote call
	 * 
	 * @return long request ID.
	 */
	public long getRequestId();
}
