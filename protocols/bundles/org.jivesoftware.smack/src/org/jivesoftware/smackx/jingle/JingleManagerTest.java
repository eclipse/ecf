/**
 * $RCSfile: JingleManagerTest.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/10/17 19:12:42 $
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

package org.jivesoftware.smackx.jingle;

import java.util.ArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.test.SmackTestCase;
import org.jivesoftware.smackx.nat.FixedResolver;
import org.jivesoftware.smackx.nat.TransportCandidate;
import org.jivesoftware.smackx.nat.TransportResolver;
import org.jivesoftware.smackx.packet.Jingle;

/**
 * Test the Jingle extension using the high level API
 * </p>
 * 
 * @author Alvaro Saurin
 */
public class JingleManagerTest extends SmackTestCase {

	private int counter;

	private final Object mutex = new Object();

	/**
	 * Constructor for JingleManagerTest.
	 * 
	 * @param name
	 */
	public JingleManagerTest(final String name) {
		super(name);

		resetCounter();
	}

	// Counter management

	private void resetCounter() {
		synchronized (mutex) {
			counter = 0;
		}
	}

	private void incCounter() {
		synchronized (mutex) {
			counter++;
		}
	}

	private int valCounter() {
		int val;
		synchronized (mutex) {
			val = counter;
		}
		return val;
	}

	/**
	 * Generate a list of payload types
	 * 
	 * @return A testing list
	 */
	private ArrayList getTestPayloads1() {
		ArrayList result = new ArrayList();

		result.add(new PayloadType.Audio(34, "supercodec-1", 2, 14000));
		result.add(new PayloadType.Audio(56, "supercodec-2", 1, 44000));
		result.add(new PayloadType.Audio(36, "supercodec-3", 2, 28000));
		result.add(new PayloadType.Audio(45, "supercodec-4", 1, 98000));

		return result;
	}

	private ArrayList getTestPayloads2() {
		ArrayList result = new ArrayList();

		result.add(new PayloadType.Audio(27, "supercodec-3", 2, 28000));
		result.add(new PayloadType.Audio(56, "supercodec-2", 1, 44000));
		result.add(new PayloadType.Audio(32, "supercodec-4", 1, 98000));
		result.add(new PayloadType.Audio(34, "supercodec-1", 2, 14000));

		return result;
	}

	private ArrayList getTestPayloads3() {
		ArrayList result = new ArrayList();

		result.add(new PayloadType.Audio(91, "badcodec-1", 2, 28000));
		result.add(new PayloadType.Audio(92, "badcodec-2", 1, 44000));
		result.add(new PayloadType.Audio(93, "badcodec-3", 1, 98000));
		result.add(new PayloadType.Audio(94, "badcodec-4", 2, 14000));

		return result;
	}

	/**
	 * Test for the session request detection. Here, we use the same filter we
	 * use in the JingleManager...
	 */
	public void testInitJingleSessionRequestListeners() {

		resetCounter();

		PacketFilter initRequestFilter = new PacketFilter() {
			// Return true if we accept this packet
			public boolean accept(Packet pin) {
				if (pin instanceof IQ) {
					IQ iq = (IQ) pin;
					if (iq.getType().equals(IQ.Type.SET)) {
						if (iq instanceof Jingle) {
							Jingle jin = (Jingle) pin;
							if (jin.getAction().equals(Jingle.Action.SESSIONINITIATE)) {
								System.out
										.println("Session initiation packet accepted... ");
								return true;
							}
						}
					}
				}
				return false;
			}
		};

		// Start a packet listener for session initiation requests
		getConnection(0).addPacketListener(new PacketListener() {
			public void processPacket(final Packet packet) {
				System.out.println("Packet detected... ");
				incCounter();
			}
		}, initRequestFilter);

		// Create a dummy packet for testing...
		IQfake iqSent = new IQfake(
				" <jingle xmlns='http://jabber.org/protocol/jingle'"
						+ " initiator=\"gorrino@viejo.com\""
						+ " responder=\"colico@hepatico.com\""
						+ " action=\"session-initiate\" sid=\"08666555\">"
						+ " <description xmlns='http://jabber.org/protocol/jingle/content/audio'>"
						+ " <payload-type id=\"34\" name=\"supercodec-34\"/>"
						+ " <payload-type id=\"23\" name=\"supercodec-23\"/>"
						+ " </description>"
						+ " <transport xmlns='http://jabber.org/protocol/jingle/transport/ice'>"
						+ " <candidate generation=\"1\"" + " ip=\"192.168.1.1\""
						+ " password=\"secret\"" + " port=\"8080\""
						+ " username=\"username\"" + " preference=\"1\"/>"
						+ " </transport>" + "</jingle>");

		iqSent.setTo(getFullJID(0));
		iqSent.setFrom(getFullJID(0));
		iqSent.setType(IQ.Type.SET);

		System.out.println("Sending packet and waiting... ");
		getConnection(0).sendPacket(iqSent);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}

		System.out.println("Awake... ");
		assertTrue(valCounter() > 0);
	}

	/**
	 * High level API test. This is a simple test to use with a XMPP client and
	 * check if the client receives the message 1. User_1 will send an
	 * invitation to user_2.
	 */
	public void testSendSimpleMessage() {

		resetCounter();

		try {
			TransportResolver tr1 = new FixedResolver("127.0.0.1", 54222);
			TransportResolver tr2 = new FixedResolver("127.0.0.1", 54567);

			JingleManager man0 = new JingleManager(getConnection(0), tr1);
			JingleManager man1 = new JingleManager(getConnection(1), tr2);

			// Session 1 waits for connections
			man1.addJingleSessionRequestListener(new JingleListener.SessionRequest() {
				/**
				 * Called when a new session request is detected
				 */
				public void sessionRequested(final JingleSessionRequest request) {
					incCounter();
					System.out.println("Session request detected, from "
							+ request.getFrom());
				}
			});

			// Session 0 starts a request
			System.out.println("Starting session request, to " + getFullJID(1) + "...");
			OutgoingJingleSession session0 = man0.createOutgoingJingleSession(
					getFullJID(1), getTestPayloads1());
			session0.start(null);

			Thread.sleep(5000);

			assertTrue(valCounter() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured with Jingle");
		}
	}

	/**
	 * High level API test. This is a simple test to use with a XMPP client and
	 * check if the client receives the message 1. User_1 will send an
	 * invitation to user_2.
	 */
	public void testAcceptJingleSession() {

		resetCounter();

		try {
			TransportResolver tr1 = new FixedResolver("127.0.0.1", 54222);
			TransportResolver tr2 = new FixedResolver("127.0.0.1", 54567);

			final JingleManager man0 = new JingleManager(getConnection(0), tr1);
			final JingleManager man1 = new JingleManager(getConnection(1), tr2);

			man1.addJingleSessionRequestListener(new JingleListener.SessionRequest() {
				/**
				 * Called when a new session request is detected
				 */
				public void sessionRequested(final JingleSessionRequest request) {
					incCounter();
					System.out.println("Session request detected, from "
							+ request.getFrom() + ": accepting.");

					// We accept the request
					IncomingJingleSession session1 = request.accept(getTestPayloads2());
					try {
						session1.start(request);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// Session 0 starts a request
			System.out.println("Starting session request, to " + getFullJID(1) + "...");
			OutgoingJingleSession session0 = man0.createOutgoingJingleSession(
					getFullJID(1), getTestPayloads1());
			session0.start(null);

			Thread.sleep(20000);

			assertTrue(valCounter() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured with Jingle");
		}
	}

	/**
	 * This is a simple test where both endpoints have exactly the same payloads
	 * and the session is accepted.
	 */
	public void testEqualPayloadsSetSession() {

		resetCounter();

		try {
			TransportResolver tr1 = new FixedResolver("127.0.0.1", 54213);
			TransportResolver tr2 = new FixedResolver("127.0.0.1", 54531);

			final JingleManager man0 = new JingleManager(getConnection(0), tr1);
			final JingleManager man1 = new JingleManager(getConnection(1), tr2);

			man1.addJingleSessionRequestListener(new JingleListener.SessionRequest() {
				/**
				 * Called when a new session request is detected
				 */
				public void sessionRequested(final JingleSessionRequest request) {
					System.out.println("Session request detected, from "
							+ request.getFrom() + ": accepting.");

					// We accept the request
					IncomingJingleSession session1 = request.accept(getTestPayloads1());

					session1.addListener(new JingleListener.Session() {
						public void sessionClosed(final String reason) {
							System.out.println("sessionClosed().");
						}

						public void sessionClosedOnError(final XMPPException e) {
							System.out.println("sessionClosedOnError().");
						}

						public void sessionDeclined(final String reason) {
							System.out.println("sessionDeclined().");
						}

						public void sessionEstablished(final PayloadType pt,
								final TransportCandidate rc, final TransportCandidate lc) {
							incCounter();
							System.out
									.println("Responder: the session is fully established.");
							System.out.println("+ Payload Type: " + pt.getId());
							System.out.println("+ Local IP/port: " + lc.getIP() + ":"
									+ lc.getPort());
							System.out.println("+ Remote IP/port: " + rc.getIP() + ":"
									+ rc.getPort());
						}

						public void sessionRedirected(final String redirection) {
						}
					});

					try {
						session1.start(request);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// Session 0 starts a request
			System.out.println("Starting session request with equal payloads, to "
					+ getFullJID(1) + "...");
			OutgoingJingleSession session0 = man0.createOutgoingJingleSession(
					getFullJID(1), getTestPayloads1());
			session0.start(null);

			Thread.sleep(20000);

			assertTrue(valCounter() == 2);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured with Jingle");
		}
	}

	/**
	 * This is a simple test where the user_2 rejects the Jingle session.
	 * 
	 */
	public void testStagesSession() {

		resetCounter();

		try {
			TransportResolver tr1 = new FixedResolver("127.0.0.1", 54222);
			TransportResolver tr2 = new FixedResolver("127.0.0.1", 54567);

			final JingleManager man0 = new JingleManager(getConnection(0), tr1);
			final JingleManager man1 = new JingleManager(getConnection(1), tr2);

			man1.addJingleSessionRequestListener(new JingleListener.SessionRequest() {
				/**
				 * Called when a new session request is detected
				 */
				public void sessionRequested(final JingleSessionRequest request) {
					System.out.println("Session request detected, from "
							+ request.getFrom() + ": accepting.");

					// We accept the request
					IncomingJingleSession session1 = request.accept(getTestPayloads2());

					session1.addListener(new JingleListener.Session() {
						public void sessionClosed(final String reason) {
							System.out.println("sessionClosed().");
						}

						public void sessionClosedOnError(final XMPPException e) {
							System.out.println("sessionClosedOnError().");
						}

						public void sessionDeclined(final String reason) {
							System.out.println("sessionDeclined().");
						}

						public void sessionEstablished(final PayloadType pt,
								final TransportCandidate rc, final TransportCandidate lc) {
							incCounter();
							System.out
									.println("Responder: the session is fully established.");
							System.out.println("+ Payload Type: " + pt.getId());
							System.out.println("+ Local IP/port: " + lc.getIP() + ":"
									+ lc.getPort());
							System.out.println("+ Remote IP/port: " + rc.getIP() + ":"
									+ rc.getPort());
						}

						public void sessionRedirected(final String redirection) {
						}
					});

					try {
						session1.start(request);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// Session 0 starts a request
			System.out.println("Starting session request, to " + getFullJID(1) + "...");
			OutgoingJingleSession session0 = man0.createOutgoingJingleSession(
					getFullJID(1), getTestPayloads1());

			session0.addListener(new JingleListener.Session() {
				public void sessionClosed(final String reason) {
				}

				public void sessionClosedOnError(final XMPPException e) {
				}

				public void sessionDeclined(final String reason) {
				}

				public void sessionEstablished(final PayloadType pt,
						final TransportCandidate rc, final TransportCandidate lc) {
					incCounter();
					System.out.println("Initiator: the session is fully established.");
					System.out.println("+ Payload Type: " + pt.getId());
					System.out.println("+ Local IP/port: " + lc.getIP() + ":"
							+ lc.getPort());
					System.out.println("+ Remote IP/port: " + rc.getIP() + ":"
							+ rc.getPort());
				}

				public void sessionRedirected(final String redirection) {
				}
			});
			session0.start(null);

			Thread.sleep(20000);

			assertTrue(valCounter() == 2);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured with Jingle");
		}
	}

	/**
	 * This is a simple test where the user_2 rejects the Jingle session.
	 */
	public void testRejectSession() {

		resetCounter();

		try {
			TransportResolver tr1 = new FixedResolver("127.0.0.1", 54222);
			TransportResolver tr2 = new FixedResolver("127.0.0.1", 54567);

			final JingleManager man0 = new JingleManager(getConnection(0), tr1);
			final JingleManager man1 = new JingleManager(getConnection(1), tr2);

			man1.addJingleSessionRequestListener(new JingleListener.SessionRequest() {
				/**
				 * Called when a new session request is detected
				 */
				public void sessionRequested(final JingleSessionRequest request) {
					System.out.println("Session request detected, from "
							+ request.getFrom() + ": rejecting.");

					// We reject the request
					request.reject();
				}
			});

			// Session 0 starts a request
			System.out.println("Starting session request, to " + getFullJID(1) + "...");
			OutgoingJingleSession session0 = man0.createOutgoingJingleSession(
					getFullJID(1), getTestPayloads1());

			session0.addListener(new JingleListener.Session() {
				public void sessionClosed(final String reason) {
				}

				public void sessionClosedOnError(final XMPPException e) {
				}

				public void sessionDeclined(final String reason) {
					incCounter();
					System.out
							.println("The session has been detected as rejected with reason: "
									+ reason);
				}

				public void sessionEstablished(final PayloadType pt,
						final TransportCandidate rc, final TransportCandidate lc) {
				}

				public void sessionRedirected(final String redirection) {
				}
			});

			session0.start(null);

			Thread.sleep(20000);

			assertTrue(valCounter() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured with Jingle");
		}
	}

	/**
	 * This is a simple test where the user_2 rejects the Jingle session.
	 */
	public void testIncompatibleCodecs() {

		resetCounter();

		try {
			TransportResolver tr1 = new FixedResolver("127.0.0.1", 54222);
			TransportResolver tr2 = new FixedResolver("127.0.0.1", 54567);

			final JingleManager man0 = new JingleManager(getConnection(0), tr1);
			final JingleManager man1 = new JingleManager(getConnection(1), tr2);

			man1.addJingleSessionRequestListener(new JingleListener.SessionRequest() {
				/**
				 * Called when a new session request is detected
				 */
				public void sessionRequested(final JingleSessionRequest request) {
					System.out.println("Session request detected, from "
							+ request.getFrom() + ": accepting.");

					// We reject the request
					IncomingJingleSession ses = request.accept(getTestPayloads3());
					try {
						ses.start(request);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// Session 0 starts a request
			System.out.println("Starting session request, to " + getFullJID(1) + "...");
			OutgoingJingleSession session0 = man0.createOutgoingJingleSession(
					getFullJID(1), getTestPayloads1());

			session0.addListener(new JingleListener.Session() {
				public void sessionClosed(final String reason) {
				}

				public void sessionClosedOnError(final XMPPException e) {
					incCounter();
					System.out
							.println("The session has been close on error with reason: "
									+ e.getMessage());
				}

				public void sessionDeclined(final String reason) {
					incCounter();
					System.out
							.println("The session has been detected as rejected with reason: "
									+ reason);
				}

				public void sessionEstablished(final PayloadType pt,
						final TransportCandidate rc, final TransportCandidate lc) {
				}

				public void sessionRedirected(final String redirection) {
				}
			});

			session0.start(null);

			Thread.sleep(20000);

			assertTrue(valCounter() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			fail("An error occured with Jingle");
		}
	}

	protected int getMaxConnections() {
		return 2;
	}

	/**
	 * Simple class for testing an IQ...
	 * 
	 * @author Alvaro Saurin
	 */
	private class IQfake extends IQ {
		private String s;

		public IQfake(final String s) {
			super();
			this.s = s;
		}

		public String getChildElementXML() {
			StringBuffer buf = new StringBuffer();
			buf.append(s);
			return buf.toString();
		}
	}
}

