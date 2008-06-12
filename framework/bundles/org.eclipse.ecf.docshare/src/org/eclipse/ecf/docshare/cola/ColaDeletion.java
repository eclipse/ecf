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

public class ColaDeletion implements TransformationStrategy {

	private static final long serialVersionUID = -7430435392915553959L;
	private static ColaDeletion INSTANCE;

	private ColaDeletion() {
		// default constructor is private to enforce singleton property via
		// static factory method
	}

	public static TransformationStrategy getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ColaDeletion();
		}
		return INSTANCE;
	}

	public ColaUpdateMessage getOperationalTransform(ColaUpdateMessage remoteIncomingMsg, ColaUpdateMessage localAppliedMsg, boolean localMsgHighPrio) {

		Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, this.getClass(), "getOperationalTransform", new Object[] {remoteIncomingMsg, localAppliedMsg, new Boolean(localMsgHighPrio)}); //$NON-NLS-1$

		// this transformation handles an incoming remote deletion that has to
		// be transformed against a locally operation
		ColaUpdateMessage remoteTransformedMsg = remoteIncomingMsg;

		if (localAppliedMsg.isInsertion()) {
			// something has been inserted at a lower or same index --> move
			// deletion right
			if (localAppliedMsg.getOffset() <= remoteTransformedMsg.getOffset()) {
				remoteTransformedMsg.setOffset(remoteTransformedMsg.getOffset() + localAppliedMsg.getText().length());
			}
		} else if (localAppliedMsg.isDeletion()) {
			if (remoteTransformedMsg.getOffset() > localAppliedMsg.getOffset()) {
				// move to remote deletion to the left
				// check for overlap
				if (remoteTransformedMsg.getOffset() < (localAppliedMsg.getOffset() + localAppliedMsg.getLength())) {
					// partial overlap on the right side of local op
					if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLength()) > (localAppliedMsg.getOffset() + localAppliedMsg.getLength())) {
						// the case that some part of the beginning of the
						// incoming
						// remote del lies within the already executed deletion
						// and extends beyond the already applied del
						// shorten the remote deletion, cut off the redundant
						// part
						// at the beginning of rem del
						remoteTransformedMsg.setLength(remoteTransformedMsg.getLength() - ((localAppliedMsg.getOffset() + localAppliedMsg.getLength()) - remoteTransformedMsg.getOffset()));
						// move shortened remote del offset to correct pos.
						remoteTransformedMsg.setOffset(localAppliedMsg.getOffset());
					} else { // full overlap, remote op fully contained
						// within local op
						// case remote deletion is fully within already applied
						// deletion, i.e. don't do anything
						remoteTransformedMsg.setLength(0); // TODO check
						remoteTransformedMsg.setOffset(0);// TODO check -
						// should resolve
						// nullpointerexc
						// whether this is
						// enough to make
						// this a no-op
					}
				} else { // no overlap
					// deletion is fully after the already applied deletion
					remoteTransformedMsg.setOffset(remoteTransformedMsg.getOffset() - localAppliedMsg.getLength());
				}
				// if incoming deletion is at a lower or equal index
			} else if (remoteTransformedMsg.getOffset() <= localAppliedMsg.getOffset()) {
				// check for overlap
				if ((remoteTransformedMsg.getOffset() + remoteIncomingMsg.getLength()) > localAppliedMsg.getOffset()) {
					// case remote op reaches into or even over the local op
					if ((remoteTransformedMsg.getOffset() + remoteIncomingMsg.getLength()) <= (localAppliedMsg.getOffset() + localAppliedMsg.getLength())) {
						// case remote op does not reach over local op, i.e.
						// shorten remote op by overlap
						remoteTransformedMsg.setLength(remoteTransformedMsg.getLength() - ((remoteIncomingMsg.getOffset() + remoteTransformedMsg.getLength()) - localAppliedMsg.getOffset()));// same
						// as
						// remoteOffset
						// - localOffset
					} else {
						// case remote op reaches over, i.e. cut out length of
						// local del-op
						remoteTransformedMsg.setLength(remoteTransformedMsg.getLength() - localAppliedMsg.getLength());
					}

				}
				// no need to do anything if there is no overlap for del-op at
				// lower index vs. local del-op
			}
		}

		remoteTransformedMsg.remoteOperationsCount += 1;
		localAppliedMsg.remoteOperationsCount += 1;

		Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "getOperationalTransform", null); //$NON-NLS-1$

		return null;
	}
}
