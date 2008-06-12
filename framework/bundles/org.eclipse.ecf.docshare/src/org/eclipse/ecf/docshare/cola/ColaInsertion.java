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
			// check for full on collision - this is comparable to equal
			// insertion
			// pos. with single chars
			if ((remoteTransformedMsg.getOffset() >= localAppliedMsg.getOffset()) && (remoteTransformedMsg.getOffset() < (localAppliedMsg.getOffset() + localAppliedMsg.getText().length()))) {
				// determine what to modify and how

				if (localMsgHighPrio) {

					int localMsgEndIndex = localAppliedMsg.getOffset() + localAppliedMsg.getText().length();
					remoteTransformedMsg.setOffset(localMsgEndIndex);

				} else {
					// localMsg if of lesser prio
					// update both operations accordingly

					remoteTransformedMsg.setOffset(localAppliedMsg.getOffset());

					// TODO is this necessary? I think so ...
					/*
					 * appliedLocalMsg.setOffset(appliedLocalMsg.getOffset() +
					 * transformedRemote.getText().length());
					 */
				}

			} else if (remoteTransformedMsg.getOffset() < localAppliedMsg.getOffset()) {

				/*
				 * appliedLocalMsg.setOffset(appliedLocalMsg.getOffset() +
				 * transformedRemote.getText().length());
				 */

			} else if (remoteTransformedMsg.getOffset() > localAppliedMsg.getOffset()) {

				remoteTransformedMsg.setOffset(remoteTransformedMsg.getOffset() + localAppliedMsg.getText().length());
			}
		} else if (localAppliedMsg.isDeletion()) {
			// TODO determine which cases are interesting to a remote insertion
			// when running into a local, already applied deletion:
			// the following seems to be the only case of relevance here
			if (localAppliedMsg.getOffset() < remoteTransformedMsg.getOffset()) {
				// move remote insertion to the left
				remoteTransformedMsg.setOffset(remoteTransformedMsg.getOffset() - localAppliedMsg.getLength());
			}
		}

		remoteTransformedMsg.remoteOperationsCount += 1;
		localAppliedMsg.remoteOperationsCount += 1;

		Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "getOperationalTransform", remoteTransformedMsg); //$NON-NLS-1$
		return remoteTransformedMsg;
	}
}
