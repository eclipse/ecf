/****************************************************************************
 * Copyright (c) 2008 Marcelo Mayworm.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: 	Marcelo Mayworm - initial API and implementation
 * 
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence.search;

import org.eclipse.ecf.core.util.Event;

/**
 * An event received by a user search. This interface address the events 
 * that happens on user search API. There be different sub-interfaces of IUserSearchEvent to
 * represent different types of events.
 * @since 2.0
 */
public interface IUserSearchEvent extends Event {
	// no methods for interface
}
