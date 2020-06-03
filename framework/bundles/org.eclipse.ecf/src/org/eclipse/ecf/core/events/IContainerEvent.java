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
package org.eclipse.ecf.core.events;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

/**
 * An event received by a container
 * 
 */
public interface IContainerEvent extends Event {
	/**
	 * Get ID of local discovery container (the discovery container receiving this event).
	 * 
	 * @return ID for local container. Will not return <code>null</code>.
	 */
	public ID getLocalContainerID();
}