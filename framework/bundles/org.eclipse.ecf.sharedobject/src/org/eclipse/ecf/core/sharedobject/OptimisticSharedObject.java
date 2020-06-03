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

import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.core.sharedobject.Activator;
import org.eclipse.ecf.internal.core.sharedobject.SharedObjectDebugOptions;

/**
 * Superclass for shared object classes that replicate themselves
 * optimistically. May be subclassed as desired.
 * 
 * @see BaseSharedObject
 */
public class OptimisticSharedObject extends BaseSharedObject {

	public OptimisticSharedObject() {
		super();
	}

	protected void initialize() throws SharedObjectInitException {
		super.initialize();
		Trace.entering(Activator.PLUGIN_ID,
				SharedObjectDebugOptions.METHODS_ENTERING,
				OptimisticSharedObject.class, "initialize"); //$NON-NLS-1$
		addEventProcessor(new IEventProcessor() {
			public boolean processEvent(Event event) {
				if (event instanceof ISharedObjectActivatedEvent) {
					// If we've been activated, are primary and are connected
					// then replicate to all remotes
					if (isPrimary() && isConnected()) {
						OptimisticSharedObject.this
								.replicateToRemoteContainers(null);
					}
				} else if (event instanceof IContainerConnectedEvent) {
					// If we've have just been connected, and are primary
					// then replicate to the newly arrived container
					if (isPrimary()) {
						ID targetID = ((IContainerConnectedEvent) event)
								.getTargetID();
						OptimisticSharedObject.this
								.replicateToRemoteContainers(new ID[] { targetID });
					}
				}
				return false;
			}
		});
		Trace.exiting(Activator.PLUGIN_ID,
				SharedObjectDebugOptions.METHODS_EXITING,
				OptimisticSharedObject.class, "initialize"); //$NON-NLS-1$
	}
}
