/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.sharedobject.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;

/**
 * Event processor to process SharedObjectMsgEvents
 * 
 * @see IEventProcessor
 * @see BaseSharedObject#addEventProcessor(IEventProcessor)
 */
public class SharedObjectMsgEventProcessor implements IEventProcessor {

	BaseSharedObject sharedObject = null;

	public SharedObjectMsgEventProcessor(BaseSharedObject sharedObject) {
		super();
		this.sharedObject = sharedObject;
	}

	public boolean processEvent(Event event) {
		if (!(event instanceof ISharedObjectMessageEvent))
			return false;
		return processSharedObjectMsgEvent((ISharedObjectMessageEvent) event);
	}

	protected boolean processSharedObjectMsgEvent(
			ISharedObjectMessageEvent event) {
		return sharedObject.handleSharedObjectMsgEvent(event);
	}
}
