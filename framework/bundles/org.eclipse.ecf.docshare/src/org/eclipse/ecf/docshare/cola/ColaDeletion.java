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

		ColaUpdateMessage remoteTransformedMsg = remoteIncomingMsg;

		if (localAppliedMsg.isDeletion()) {
			final int noOpLength = 0;

			if (remoteTransformedMsg.getOffset() < localAppliedMsg.getOffset()) {

				if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) < localAppliedMsg.getOffset()) {

					//no overlap - remote OK as is, local needs modification
					localAppliedMsg.setOffset(localAppliedMsg.getOffset() - remoteTransformedMsg.getLengthOfReplacedText());

				} else if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) < (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText())) {

					//remote del at lower index reaches into locally applied del, local del reaches further out
					//--> shorten remote del appropriately, move and shorten local del left
					remoteTransformedMsg.setLengthOfReplacedText((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) - localAppliedMsg.getOffset());
					localAppliedMsg.setLengthOfReplacedText((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) - localAppliedMsg.getOffset());
					localAppliedMsg.setOffset(remoteTransformedMsg.getOffset());//TODO verify!

				} else if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) >= (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText())) {

					//remote del at lower index, remote del fully extends over local del
					//--> shorten remote by local.lengthOfReplacedText, make local no-op
					remoteTransformedMsg.setLengthOfReplacedText(remoteTransformedMsg.getLengthOfReplacedText() - localAppliedMsg.getLengthOfReplacedText());
					//TODO verify whether this is equivalent to setting no-op, otherwise declare a noop boolean field for ColaDeletions
					localAppliedMsg.setOffset(remoteTransformedMsg.getOffset());
					localAppliedMsg.setLengthOfReplacedText(noOpLength);
				}
			} else if (remoteTransformedMsg.getOffset() == localAppliedMsg.getOffset()) {

				if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) < (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText())) {
					//start indices are equal, remote is shorter --> make remote no-op
					remoteTransformedMsg.setLengthOfReplacedText(noOpLength);
					//--> shorten localOp to only delete non-overlapping region
					localAppliedMsg.setLengthOfReplacedText(localAppliedMsg.getLengthOfReplacedText() - remoteTransformedMsg.getLengthOfReplacedText());
				} else if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) == (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText())) {
					//same endIndex, i.e. same deletion
					//--> make remote op be no-op
					remoteTransformedMsg.setLengthOfReplacedText(noOpLength);
					//--> make local op appear as no-op
					localAppliedMsg.setLengthOfReplacedText(noOpLength);
				} else if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) > (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText())) {
					//remote del extends over local del
					//-->shorten remote del by length of local del, index/offset does not need to be updated
					remoteTransformedMsg.setLengthOfReplacedText(remoteTransformedMsg.getLengthOfReplacedText() - localAppliedMsg.getLengthOfReplacedText());
					//-->make local del appear as no-op
					localAppliedMsg.setLengthOfReplacedText(noOpLength);
				}
			} else if (remoteTransformedMsg.getOffset() > localAppliedMsg.getOffset()) {

				if (remoteTransformedMsg.getOffset() > (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText())) {

					//move remote deletion left by length of local deletion
					remoteTransformedMsg.setOffset(remoteTransformedMsg.getOffset() - localAppliedMsg.getLengthOfReplacedText());

				} else if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) < (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText())) {

					//remote is fully contained in/overlapping with local del
					//--> remote is no-op
					remoteTransformedMsg.setLengthOfReplacedText(noOpLength);
					//-->local needs to be shortened by length of remote
					localAppliedMsg.setLengthOfReplacedText(localAppliedMsg.getLengthOfReplacedText() - remoteTransformedMsg.getLengthOfReplacedText());

				} else if (remoteTransformedMsg.getOffset() < (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText())) {

					//remote del starts within local del, but extends further
					//-->shorten remote by overlap and move left to index of local del
					remoteTransformedMsg.setLengthOfReplacedText(remoteTransformedMsg.getLengthOfReplacedText() - (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText()) - remoteTransformedMsg.getOffset());
					remoteTransformedMsg.setOffset(localAppliedMsg.getOffset());
					//-->shorten local applied message
					localAppliedMsg.setLengthOfReplacedText(localAppliedMsg.getLengthOfReplacedText() - (localAppliedMsg.getOffset() + localAppliedMsg.getLengthOfReplacedText()) - remoteTransformedMsg.getOffset());
				}
			}
		} else if (localAppliedMsg.isInsertion()) {
			if (remoteTransformedMsg.getOffset() < localAppliedMsg.getOffset()) {
				if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) < localAppliedMsg.getOffset()) {
					//remote remains unchanged, deletion happens fully before local insertion
					//local insertion needs to be moved left by full length of deletion
					localAppliedMsg.setOffset(localAppliedMsg.getOffset() - remoteTransformedMsg.getLengthOfReplacedText());
				} else if ((remoteTransformedMsg.getOffset() + remoteTransformedMsg.getLengthOfReplacedText()) >= localAppliedMsg.getOffset()) {
					//remote deletion reaches into local insertion and potentially over it
					//local insertion needs to be moved left by overlap
					//remote deletion needs to be split apart
					//TODO needs to be implemented and requires appropriate modification in ColaSynchronizer
					throw new IllegalArgumentException("NOT IMPLEMENTED OPERATIONAL TRANSFORM");
				}
			} else if (remoteTransformedMsg.getOffset() >= localAppliedMsg.getOffset()) {
				//remote del needs to be moved right by full length of insertion
				remoteTransformedMsg.setOffset(remoteTransformedMsg.getOffset() + localAppliedMsg.getLengthOfInsertedText());
			}
		}

		Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "getOperationalTransform", null); //$NON-NLS-1$

		return remoteTransformedMsg;
	}

}
