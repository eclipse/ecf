/**
 * $RCSfile: TransportResolver.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/17 19:13:55 $
 *
 * Copyright (C) 2002-2006 Jive Software. All rights reserved.
 * ====================================================================
 * The Jive Software License (based on Apache Software License, Version 1.1)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Jive Software (http://www.jivesoftware.com)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Smack" and "Jive Software" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please
 *    contact webmaster@jivesoftware.com.
 *
 * 5. Products derived from this software may not be called "Smack",
 *    nor may "Smack" appear in their name, without prior written
 *    permission of Jive Software.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL JIVE SOFTWARE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

package org.jivesoftware.smackx.nat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.XMPPException;

/**
 * A TransportResolver is used for obtaining a list of valid transport
 * candidates.
 * 
 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
 */
public abstract class TransportResolver {

	// the time, in milliseconds, before a check aborts
	public static final int CHECK_TIMEOUT = 2000;

	// Listeners for events
	private final ArrayList listeners = new ArrayList();

	// TRue if the resolver is working
	private boolean resolving;

	// This will be true when all the transport candidates have been gathered...
	private boolean resolved;

	// We store a list of candidates internally, just in case there are several
	// possibilities. When the user asks for a transport, we return the best
	// one.
	protected final List candidates = new ArrayList();

	// Remote candidates that are being checked
	private final static ArrayList candidatesChecking = new ArrayList();

	/**
	 * Default constructor.
	 */
	protected TransportResolver() {
		super();

		resolving = false;
		resolved = false;
	}

	/**
	 * Start the resolution.
	 */
	public abstract void resolve() throws XMPPException;

	/**
	 * Clear the list of candidates and start a new resolution process.
	 * 
	 * @throws XMPPException
	 */
	public void clear() throws XMPPException {
		cancel();
		candidates.clear();
		resolve();
	}

	/**
	 * Cancel any asynchronous resolution operation.
	 */
	public abstract void cancel() throws XMPPException;

	/**
	 * Return true if the resolver is working.
	 * 
	 * @return true if the resolver is working.
	 */
	public boolean isResolving() {
		return resolving;
	}

	/**
	 * Return true if the resolver has finished the search for transport
	 * candidates.
	 * 
	 * @return true if the search has finished
	 */
	public boolean isResolved() {
		return resolved;
	}

	/**
	 * Indicate the beggining of the resolution process. This method must be
	 * used by subclasses at the begining of their resolve() method.
	 */
	protected synchronized void setResolveInit() {
		resolved = false;
		resolving = true;

		triggerResolveInit();
	}

	/**
	 * Indicate the end of the resolution process. This method must be used by
	 * subclasses at the begining of their resolve() method.
	 */
	protected synchronized void setResolveEnd() {
		resolved = true;
		resolving = false;

		triggerResolveEnd();
	}

	/**
	 * Check if a transport candidate is usable. The transport resolver should
	 * check if the transport candidate the other endpoint has provided is
	 * usable.
	 * 
	 * This method provides a basic check where it sends a "ping" to the remote
	 * address provided in the candidate. If the "ping" succedess, the candidate
	 * is accepted. Subclasses should provide better methods if they can...
	 * 
	 * @param cand The transport candidate to test.
	 */
	public void check(final TransportCandidate cand) {
		if (!candidatesChecking.contains(cand)) {
			candidatesChecking.add(cand);

			Thread checkThread = new Thread(new Runnable() {
				public void run() {
					boolean isUsable;

					InetAddress candAddress;
					try {
						candAddress = InetAddress.getByName(cand.getIP());
						isUsable = candAddress.isReachable(CHECK_TIMEOUT);
					} catch (Exception e) {
						isUsable = false;
					}
					triggerCandidateChecked(cand, isUsable);

					candidatesChecking.remove(cand);
				}
			}, "Transport candidate check");

			checkThread.setName("Transport candidate test");
			checkThread.start();
		}
	}

	// Listeners management

	/**
	 * Add a transport resolver listener.
	 * 
	 * @param li The transport resolver listener to be added.
	 */
	public void addListener(final TransportResolverListener li) {
		synchronized (listeners) {
			listeners.add(li);
		}
	}

	/**
	 * Removes a transport resolver listener.
	 * 
	 * @param li The transport resolver listener to be removed
	 */
	public void removeListener(final TransportResolverListener li) {
		synchronized (listeners) {
			listeners.remove(li);
		}
	}

	/**
	 * Get the list of listeners
	 * 
	 * @return the list of listeners
	 */
	public ArrayList getListenersList() {
		synchronized (listeners) {
			return new ArrayList(listeners);
		}
	}

	/**
	 * Trigger a new candidate added event.
	 * 
	 * @param cand The candidate added to the list of candidates.
	 */
	protected void triggerCandidateAdded(final TransportCandidate cand) {
		Iterator iter = getListenersList().iterator();
		while (iter.hasNext()) {
			TransportResolverListener trl = (TransportResolverListener) iter.next();
			if (trl instanceof TransportResolverListener.Resolver) {
				TransportResolverListener.Resolver li = (TransportResolverListener.Resolver) trl;
				li.candidateAdded(cand);
			}
		}
	}

	/**
	 * Trigger a new candidate checked event.
	 * 
	 * @param cand The checked candidate.
	 * @param result The result.
	 */
	protected void triggerCandidateChecked(final TransportCandidate cand,
			final boolean result) {
		Iterator iter = getListenersList().iterator();
		while (iter.hasNext()) {
			TransportResolverListener trl = (TransportResolverListener) iter.next();
			if (trl instanceof TransportResolverListener.Checker) {
				TransportResolverListener.Checker li = (TransportResolverListener.Checker) trl;
				li.candidateChecked(cand, result);
			}
		}
	}

	/**
	 * Trigger a event notifying the initialization of the resolution process.
	 */
	private void triggerResolveInit() {
		Iterator iter = getListenersList().iterator();
		while (iter.hasNext()) {
			TransportResolverListener trl = (TransportResolverListener) iter.next();
			if (trl instanceof TransportResolverListener.Resolver) {
				TransportResolverListener.Resolver li = (TransportResolverListener.Resolver) trl;
				li.init();
			}
		}
	}

	/**
	 * Trigger a event notifying the obtention of all the candidates.
	 */
	private void triggerResolveEnd() {
		Iterator iter = getListenersList().iterator();
		while (iter.hasNext()) {
			TransportResolverListener trl = (TransportResolverListener) iter.next();
			if (trl instanceof TransportResolverListener.Resolver) {
				TransportResolverListener.Resolver li = (TransportResolverListener.Resolver) trl;
				li.end();
			}
		}
	}

	// Candidates management

	/**
	 * Clear the list of candidate
	 */
	protected void clearCandidates() {
		synchronized (candidates) {
			candidates.clear();
		}
	}

	/**
	 * Add a new transport candidate
	 * 
	 * @param cand The candidate to add
	 */
	protected void addCandidate(final TransportCandidate cand) {
		synchronized (candidates) {
			candidates.add(cand);
		}

		// Notify the listeners
		triggerCandidateAdded(cand);
	}

	/**
	 * Get an iterator for the list of candidates
	 * 
	 * @return an iterator
	 */
	public Iterator getCandidates() {
		synchronized (candidates) {
			return Collections.unmodifiableList(new ArrayList(candidates)).iterator();
		}
	}

	/**
	 * Get the candididate with the highest preference.
	 * 
	 * @return The best candidate, according to the preference order.
	 */
	public TransportCandidate getPreferredCandidate() {
		TransportCandidate result = null;

		ArrayList cands = (ArrayList) getCandidatesList();
		if (cands.size() > 0) {
			Collections.sort(cands);
			// Return the last candidate
			result = (TransportCandidate) cands.get(cands.size() - 1);
		}

		return result;
	}

	/**
	 * Get the numer of transport candidates.
	 * 
	 * @return The length of the transport candidates list.
	 */
	public int getCandidateCount() {
		synchronized (candidates) {
			return candidates.size();
		}
	}

	/**
	 * Get the list of candidates
	 * 
	 * @return the list of transport candidates
	 */
	public List getCandidatesList() {
		ArrayList result = null;

		synchronized (candidates) {
			result = new ArrayList(candidates);
		}

		return result;
	}

	/**
	 * Get the n-th candidate
	 * 
	 * @return a transport candidate
	 */
	public TransportCandidate getCandidate(final int i) {
		TransportCandidate cand;

		synchronized (candidates) {
			cand = (TransportCandidate) candidates.get(i);
		}
		return cand;
	}
}
