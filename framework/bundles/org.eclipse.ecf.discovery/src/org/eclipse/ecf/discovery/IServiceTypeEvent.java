/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
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

package org.eclipse.ecf.discovery;

import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

/**
 * Service type discovery event that provides access to service type
 */
public interface IServiceTypeEvent extends IContainerEvent {

	/**
	 * Get service type id for this service type event.
	 * @return IServiceTypeID for this service type event.  Will not be <code>null</code>.
	 */
	public IServiceTypeID getServiceTypeID();
}
