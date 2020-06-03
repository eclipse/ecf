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

package org.eclipse.ecf.discovery;

import org.eclipse.ecf.core.events.IContainerEvent;

/**
 * Service discovery event that provides access to IServiceInfo instance
 */
public interface IServiceEvent extends IContainerEvent {
	/**
	 * Get the service info associated with this event
	 * 
	 * @return IServiceInfo any info associated with this event. May be <code>null</code>.
	 */
	public IServiceInfo getServiceInfo();
}
