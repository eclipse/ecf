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

package org.eclipse.ecf.internal.provisional.docshare.cola;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.docshare.Activator;
import org.eclipse.ecf.internal.docshare.DocshareDebugOptions;
import org.eclipse.ecf.internal.provisional.docshare.messages.UpdateMessage;

public class ColaUpdateMessage extends UpdateMessage {

	private static final long serialVersionUID = 2038025022180647210L;

	// TODO encapsulate in a new ColaOpOriginationState and re-implement equals,
	// hashCode, i.e. make comparable
	private final long localOperationsCount;
	private long remoteOperationsCount;
	private final TransformationStrategy trafoStrat;
	private boolean splitUp;
	private List splitUpRepresentation;

	public ColaUpdateMessage(UpdateMessage msg, long localOperationsCount, long remoteOperationsCount) {
		super(msg.getOffset(), msg.getLengthOfReplacedText(), msg.getText());
		this.localOperationsCount = localOperationsCount;
		this.remoteOperationsCount = remoteOperationsCount;
		this.splitUp = false;
		this.splitUpRepresentation = new LinkedList();
		if (super.getLengthOfReplacedText() == 0) {
			// this is neither a replacement, nor a deletion
			trafoStrat = ColaInsertion.getInstance();
		} else {
			if (super.getText().length() == 0) {
				// something has been replaced, nothing inserted, must be a
				// deletion
				trafoStrat = ColaDeletion.getInstance();
			} else {
				// something has been replaced with some new input, has to be a
				// replacement op
				trafoStrat = ColaReplacement.getInstance();
				//TODO this has not been implemented yet
				throw new IllegalArgumentException("Replacement Handling not implemented yet! Known Bug.");
			}
		}
	}

	public boolean isInsertion() {
		return (this.trafoStrat instanceof ColaInsertion);
	}

	public boolean isDeletion() {
		return (this.trafoStrat instanceof ColaDeletion);
	}

	public long getLocalOperationsCount() {
		return this.localOperationsCount;
	}

	public long getRemoteOperationsCount() {
		return this.remoteOperationsCount;
	}

	public ColaUpdateMessage transformAgainst(ColaUpdateMessage localMsg, boolean localMsgHighPrio) {
		Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, this.getClass(), "transformAgainst", localMsg); //$NON-NLS-1$
		ColaUpdateMessage transformedMsg = trafoStrat.getOperationalTransform(this, localMsg, localMsgHighPrio);
		Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "transformAgainst", transformedMsg); //$NON-NLS-1$
		return transformedMsg;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ColaUpdateMessage["); //$NON-NLS-1$
		buf.append("text=").append(getText()).append(";offset=").append(getOffset()); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(";length=").append(getLengthOfReplacedText()).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(";operationsCount[local=").append(getLocalOperationsCount()); //$NON-NLS-1$
		buf.append(";remote=").append(getRemoteOperationsCount()).append("]]"); //$NON-NLS-1$//$NON-NLS-2$
		return buf.toString();
	}

	public void setSplitUp(boolean toBeSplitUp) {
		this.splitUp = toBeSplitUp;
	}

	public boolean isSplitUp() {
		return splitUp;
	}

	public void setSplitUpRepresentation(List splitUpRepresentation) {
		this.splitUpRepresentation = splitUpRepresentation;
	}

	public List getSplitUpRepresentation() {
		return splitUpRepresentation;
	}

	public void addToSplitUpRepresentation(ColaUpdateMessage splitUpRepresentationPart) {
		this.splitUpRepresentation.add(splitUpRepresentationPart);
	}
}
