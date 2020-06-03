/****************************************************************************
 * Copyright (c) 2013 Markus Alexander Kuppe and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Markus Alexander Kuppe - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice.eventadmin;

import org.osgi.service.event.Event;

public class TestDistributedEventAdmin extends DistributedEventAdmin {

	public void setIgnoreSerializationFailures(boolean ignore) {
		DistributedEventAdmin.ignoreSerializationExceptions = ignore;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.remoteservice.eventadmin.DistributedEventAdmin#sendMessage(org.osgi.service.event.Event)
	 */
	@Override
	public void sendMessage(Event eventToSend) {
		super.sendMessage(eventToSend);
	}

}
