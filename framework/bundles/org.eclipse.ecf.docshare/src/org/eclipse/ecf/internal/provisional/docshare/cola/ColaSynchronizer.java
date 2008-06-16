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

import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.docshare.Activator;
import org.eclipse.ecf.internal.docshare.DocshareDebugOptions;
import org.eclipse.ecf.internal.provisional.docshare.DocShare;
import org.eclipse.ecf.internal.provisional.docshare.SynchronizationStrategy;
import org.eclipse.ecf.internal.provisional.docshare.messages.UpdateMessage;
import org.eclipse.osgi.util.NLS;

public class ColaSynchronizer implements SynchronizationStrategy {

	// <ColaUpdateMessage>
	private final LinkedList unacknowledgedLocalOperations;
	private final boolean isInitiator;
	private long localOperationsCount;
	private long remoteOperationsCount;

	// <DocShare, ColaSynchronizer>
	private static Map sessionStrategies = new HashMap();

	private ColaSynchronizer(DocShare docshare) {
		this.isInitiator = docshare.isInitiator();
		unacknowledgedLocalOperations = new LinkedList();
		localOperationsCount = 0;
		remoteOperationsCount = 0;
	}

	public static ColaSynchronizer getInstanceFor(DocShare docshare) {
		if (sessionStrategies.get(docshare) == null) {
			sessionStrategies.put(docshare, new ColaSynchronizer(docshare));
		}
		return (ColaSynchronizer) sessionStrategies.get(docshare);
	}

	public static void cleanUpFor(DocShare docshare) {
		sessionStrategies.remove(docshare);
	}

	public UpdateMessage registerOutgoingMessage(UpdateMessage localMsg) {
		Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, this.getClass(), "registerOutgoingMessage", localMsg); //$NON-NLS-1$
		final ColaUpdateMessage colaMsg = new ColaUpdateMessage(localMsg, localOperationsCount, remoteOperationsCount);
		if (!colaMsg.isReplacement()) {
			unacknowledgedLocalOperations.add(colaMsg);
			localOperationsCount++;
		}
		Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "registerOutgoingMessage", colaMsg); //$NON-NLS-1$
		return colaMsg;
	}

	/**
	 * Handles proper transformation of incoming <code>ColaUpdateMessage</code>s.
	 * Returned <code>UpdateMessage</code>s can be applied directly to the
	 * shared document. The method implements the concurrency algorithm described
	 * in <code>http://wiki.eclipse.org/RT_Shared_Editing</code>
	 * @param remoteMsg 
	 * @return List contains <code>UpdateMessage</code>s ready for sequential application to document
	 */
	public List transformIncomingMessage(final UpdateMessage remoteMsg) {
		if (!(remoteMsg instanceof ColaUpdateMessage)) {
			throw new IllegalArgumentException("UpdateMessage is incompatible with Cola SynchronizationStrategy"); //$NON-NLS-1$
		}
		Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, this.getClass(), "transformIncomingMessage", remoteMsg); //$NON-NLS-1$
		ColaUpdateMessage transformedRemote = (ColaUpdateMessage) remoteMsg;

		List transformedRemotes = new LinkedList();
		transformedRemotes.add(transformedRemote);

		remoteOperationsCount++;
		//this is where the concurrency algorithm is executed
		if (!unacknowledgedLocalOperations.isEmpty()) {//Do not remove this. It is necessary. The following iterator does not suffice.
			// remove operations from queue that have been implicitly
			// acknowledged as received on the remote site by the reception of
			// this message
			for (final Iterator it = unacknowledgedLocalOperations.iterator(); it.hasNext();) {
				ColaUpdateMessage unackedLocalOp = (ColaUpdateMessage) it.next();
				if (transformedRemote.getRemoteOperationsCount() > unackedLocalOp.getLocalOperationsCount()) {
					Trace.trace(Activator.PLUGIN_ID, NLS.bind("transformIncomingMessage.removing {0}", unackedLocalOp)); //$NON-NLS-1$
					it.remove();
				} else {
					// the unackowledgedLocalOperations queue is ordered and
					// sorted
					// due to sequential insertion of local ops, thus once a
					// local op with a higher
					// or equal local op count (i.e. remote op count from the
					// remote operation's view)
					// is reached, we can abandon the check for the remaining
					// queue items
					Trace.trace(Activator.PLUGIN_ID, "breaking out of unackedLocalOperations loop"); //$NON-NLS-1$
					break;// exits for-loop
				}
			}

			// at this point the queue has been freed of operations that
			// don't require to be transformed against

			if (!unacknowledgedLocalOperations.isEmpty()) {
				ColaUpdateMessage localOp = (ColaUpdateMessage) unacknowledgedLocalOperations.getFirst();
				Assert.isTrue(transformedRemote.getRemoteOperationsCount() == localOp.getLocalOperationsCount());

				for (final ListIterator unackOpsListIt = unacknowledgedLocalOperations.listIterator(); unackOpsListIt.hasNext();) {
					for (final ListIterator trafoRemotesIt = transformedRemotes.listIterator(); trafoRemotesIt.hasNext();) {
						// returns new instance
						// clarify operation preference, owner/docshare initiator
						// consistently comes first
						localOp = (ColaUpdateMessage) unackOpsListIt.next();
						transformedRemote = (ColaUpdateMessage) trafoRemotesIt.next();
						transformedRemote = transformedRemote.transformAgainst(localOp, isInitiator);

						if (transformedRemote.isSplitUp()) {
							//currently this only happens for a remote deletion that needs to be transformed against a locally applied insertion
							//attention: before applying a list of deletions to docshare, the indices need to be updated/finalized one last time
							//since deletions can only be applied sequentially and every deletion is going to change the underlying document and the
							//respective indices!
							trafoRemotesIt.remove();
							for (final Iterator splitUpIterator = transformedRemote.getSplitUpRepresentation().iterator(); splitUpIterator.hasNext();) {
								trafoRemotesIt.add(splitUpIterator.next());
							}
							//according to the ListIterator documentation it seems so as if the following line is unnecessary, 
							//as a call to next() after the last removal and additions will return what it would have returned anyway 
							//trafoRemotesIt.next();//TODO not sure about the need for this - I want to jump over the two inserted ops and reach the end of this iterator
						}

						//TODO check whether or not this collection shuffling does what it is supposed to, i.e. remove current localop in unack list and add split up representation instead
						if (localOp.isSplitUp()) {
							//local operation has been split up during operational transform --> remove current version and add new versions plus jump over those
							unackOpsListIt.remove();
							for (final Iterator splitUpOpIterator = localOp.getSplitUpRepresentation().iterator(); splitUpOpIterator.hasNext();) {
								unackOpsListIt.add(splitUpOpIterator.next());
							}
							//according to the ListIterator documentation it seems so as if the following line is unnecessary, 
							//as a call to next() after the last removal and additions will return what it would have returned anyway
							//unackOpsListIt.next();//TODO check whether or not this does jump over both inserted operations that replaced the current unack op
						}//end split up localop handling
					}//transformedRemotes List iteration	
				}
			}

		}
		Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "transformIncomingMessage", transformedRemote); //$NON-NLS-1$

		//TODO find a cleaner and more OO way of cleaning up the list if it contains multiple deletions:
		if (transformedRemotes.size() > 1) {
			ColaUpdateMessage firstOp = (ColaUpdateMessage) transformedRemotes.get(0);
			if (firstOp.isDeletion()) {
				//this means all operations in the return list must also be deletions, i.e. modify virtual/optimistic offset for sequential application to document
				ListIterator deletionFinalizerIt = transformedRemotes.listIterator();
				ColaUpdateMessage previousDel = (ColaUpdateMessage) deletionFinalizerIt.next();//jump over first del-op does not need modification, we know this is OK because of previous size check;
				ColaUpdateMessage currentDel;

				for (; deletionFinalizerIt.hasNext();) {
					currentDel = (ColaUpdateMessage) deletionFinalizerIt.next();
					currentDel.setOffset(currentDel.getOffset() - previousDel.getLengthOfReplacedText());
					previousDel = currentDel;
				}
			}
		}

		return transformedRemotes;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("ColaSynchronizer"); //$NON-NLS-1$
		return buf.toString();
	}
}