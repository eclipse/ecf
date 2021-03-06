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
package org.eclipse.ecf.core.sharedobject.events;

import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;

/**
 * Shared object manager event
 * 
 */
public interface ISharedObjectManagerEvent extends IContainerEvent {

	/**
	 * Get shared object ID for shared object in question
	 * 
	 * @return ID of shared object in question. Will not return null.
	 */
	public ID getSharedObjectID();
}
