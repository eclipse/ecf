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

/**
 * Container disconnected event
 * 
 */
public interface IContainerDisconnectedEvent extends IContainerEvent {
	/**
	 * Get ID of container target (the container we were disconnected from)
	 * 
	 * @return ID the ID of the container were disconnected from. Will not be
	 *         null.
	 */
	public ID getTargetID();
}