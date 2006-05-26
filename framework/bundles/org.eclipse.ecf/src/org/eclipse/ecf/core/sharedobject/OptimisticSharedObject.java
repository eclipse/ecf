/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.internal.core.Trace;

/**
 * Superclass for shared object classes that replicate themselves
 * optimistically.
 * 
 */
public class OptimisticSharedObject extends AbstractSharedObject {

	public static final Trace trace = Trace.create("optimisticsharedobject");
	public OptimisticSharedObject() {
		super();
	}
	protected void trace(String msg) {
		if (Trace.ON && trace != null) {
			trace.msg(getID() + ":"
					+ (isPrimary() ? "primary:" : "replica:")
					+ msg);
		}
	}
	protected void traceStack(String msg, Throwable t) {
		if (Trace.ON && trace != null) {
			trace.dumpStack(t, getID() + ":"
					+ (isPrimary() ? "primary" : "replica")
					+ msg);
		}
	}

	protected void initialize() throws SharedObjectInitException {
		super.initialize();
		trace("initialize()");
		addEventProcessor(new IEventProcessor() {
			public boolean acceptEvent(Event event) {
				return true;
			}
			public Event processEvent(Event event) {
				if (event instanceof ISharedObjectActivatedEvent) {
					ISharedObjectActivatedEvent soae = (ISharedObjectActivatedEvent) event;
					if (isPrimary() && isConnected()) {
						trace("replicating to all");
						OptimisticSharedObject.this
						.replicateToRemoteContainers(null);
					}
				} else if (event instanceof IContainerConnectedEvent) {
					if (isPrimary()) {
						ID targetID = ((IContainerConnectedEvent) event)
						.getTargetID();
						trace("replicating to target="+targetID);
						OptimisticSharedObject.this
								.replicateToRemoteContainers(new ID[] { targetID });
					}
				}
				return event;
			}
		});
	}
}
