/****************************************************************************
 * Copyright (c) 2008 Mustafa K. Isik and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mustafa K. Isik - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.docshare.cola;

import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.docshare.Activator;
import org.eclipse.ecf.internal.docshare.DocshareDebugOptions;

public class ColaInsertion implements TransformationStrategy {

	private static final long serialVersionUID = 5192625383622519749L;
	private static ColaInsertion INSTANCE;

	private ColaInsertion() {
		// default constructor is private to enforce singleton property via
		// static factory method
	}

	public static TransformationStrategy getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ColaInsertion();
		}
		return INSTANCE;
	}

	public ColaUpdateMessage getOperationalTransform(ColaUpdateMessage remoteIncomingMsg, ColaUpdateMessage localAppliedMsg, boolean localMsgHighPrio) {

		Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, this.getClass(), "getOperationalTransform", new Object[] {remoteIncomingMsg, localAppliedMsg, new Boolean(localMsgHighPrio)}); //$NON-NLS-1$

		ColaUpdateMessage remoteTransformedMsg = remoteIncomingMsg;

		if (localAppliedMsg.isInsertion()) {

			if (remoteTransformedMsg.getOffset() < localAppliedMsg.getOffset()) {
				//coopt(remote(low),local(high)) --> (remote(low),local(low + high))
				localAppliedMsg.setOffset(localAppliedMsg.getOffset() + remoteTransformedMsg.getOffset());
			} else if (remoteTransformedMsg.getOffset() == localAppliedMsg.getOffset()) {
				if (localMsgHighPrio) {
					remoteTransformedMsg.setOffset(remoteTransformedMsg.getOffset() + localAppliedMsg.getText().length());
				} else {
					localAppliedMsg.setOffset(localAppliedMsg.getOffset() + remoteTransformedMsg.getText().length());
				}
			} else if (remoteTransformedMsg.getOffset() > localAppliedMsg.getOffset()) {
				remoteTransformedMsg.setOffset(remoteTransformedMsg.getOffset() + localAppliedMsg.getText().length());
			}
		}

		Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "getOperationalTransform", remoteTransformedMsg); //$NON-NLS-1$
		return remoteTransformedMsg;
	}
}
