/*
 * (C) Copyright 2005 - 2006 Work2gather SAS <http://www.work2gather.com> and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Work2gather - initial API and implementation
 * 
 */

package org.eclipse.ecf.provider.xmpp.jingle;

import java.util.ArrayList;

import org.eclipse.ecf.call.CallException;
import org.eclipse.ecf.call.ICallContainerAdapter;
import org.eclipse.ecf.call.ICallDescription;
import org.eclipse.ecf.call.ICallSession;
import org.eclipse.ecf.call.ICallSessionListener;
import org.eclipse.ecf.call.ICallTransportCandidate;
import org.eclipse.ecf.call.events.ICallSessionEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ECFException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.jingle.IncomingJingleSession;
import org.jivesoftware.smackx.jingle.JingleListener;
import org.jivesoftware.smackx.jingle.JingleManager;
import org.jivesoftware.smackx.jingle.JingleSessionRequest;
import org.jivesoftware.smackx.jingle.OutgoingJingleSession;
import org.jivesoftware.smackx.jingle.PayloadType;
import org.jivesoftware.smackx.nat.STUNResolver;
import org.jivesoftware.smackx.nat.TransportResolver;

/**
 * @author Pierre-Henry Perret
 *
 */
public class XMPPJingleContainer implements ICallContainerAdapter, ICallSessionListener {

	private TransportResolver tm= null;
	private XMPPConnection conn= null;
	private JingleManager jm= null;
	private String jID= null;
	
	public XMPPJingleContainer( XMPPConnection conn ){
		this.conn= conn;
		tm= new STUNResolver();
		jm= new JingleManager( conn , tm );
		jID= conn.getConnectionID();
		
		// install request listener for incoming sessions
		jm.addJingleSessionRequestListener(new JingleListener.SessionRequest() {
			/**
			 * Called when a new session request is detected
			 */
			public void sessionRequested(final JingleSessionRequest request) {
				System.out.println("Session request detected, from "+ request.getFrom());
				// We accept the request
				IncomingJingleSession session1 = request.accept(getTestPayloads());
				try {
					session1.start(request);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.call.ICallContainerAdapter#addListener(org.eclipse.ecf.call.ICallSessionListener)
	 */
	public void addListener(ICallSessionListener listener) {
		

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.call.ICallContainerAdapter#createCallSession()
	 */
	public ICallSession createCallSession() {
		
		
		System.out.println("Starting session request, to " + jID + "...");
		final OutgoingJingleSession session0 = jm.createOutgoingJingleSession(
				jID, getTestPayloads());
		session0.start(null);

		return new ICallSession() {

			public State getCallSessionState() {
				// jingle is in state OutgoingJingleSession.Inviting
				return ICallSession.State.PREPENDING;
			}

			public ID getInitiator() {
				// TODO Auto-generated method stub
				return null;
			}

			public ID getReceiver() {
				// TODO Auto-generated method stub
				return null;
			}

			public void sendInitiate(ID initiator, ID receiver, ICallDescription[] descriptions, ICallTransportCandidate[] transports) throws CallException {
				// TODO Auto-generated method stub
				
			}

			public void sendTerminate() throws CallException {
				// TODO Auto-generated method stub
				
			}

			public ID getID() {
				// TODO Auto-generated method stub
				return null;
			}

			public Object getAdapter(Class adapter) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	public ICallSession createCallSession(ID sessionID) throws ECFException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICallSession getCallSession(ID callSessionID) {
		// TODO Auto-generated method stub
		return null;
	}

	public Namespace getCallSessionNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean removeCallSession(ID callSessionID) {
		// TODO Auto-generated method stub
		return false;
	}

	public void handleCallSessionEvent(ICallSessionEvent event) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Generate a list of payload types
	 * 
	 * @return A testing list
	 */
	private ArrayList getTestPayloads() {
		ArrayList result = new ArrayList();
	
		result.add(new PayloadType.Audio(34, "supercodec-1", 2, 14000));
		result.add(new PayloadType.Audio(56, "supercodec-2", 1, 44000));
		result.add(new PayloadType.Audio(36, "supercodec-3", 2, 28000));
		result.add(new PayloadType.Audio(45, "supercodec-4", 1, 98000));
	
		return result;
	}

	public void removeListener(ICallSessionListener listener) {
		// TODO Auto-generated method stub
		
	}

}
