package org.jivesoftware.smackx.nat;

import org.jivesoftware.smack.XMPPException;

/**
 * Specialization of the BasicResolver. The FixedResolver is a resolver where
 * the external address and port are previously known when the object is
 * initialized.
 * 
 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
 */
public class FixedResolver extends BasicResolver {

	TransportCandidate fixedCandidate;

	/**
	 * Constructor.
	 */
	public FixedResolver(final String ip, final int port) {
		super();
		setFixedCandidate(ip, port);
	}

	/**
	 * Create a basic resolver, where we provide the IP and port.
	 * 
	 * @param ip an IP address
	 * @param port a port
	 */
	public void setFixedCandidate(final String ip, final int port) {
		fixedCandidate = new TransportCandidate.Fixed(ip, port);
	}

	/**
	 * Resolve the IP address.
	 */
	public synchronized void resolve() throws XMPPException {

		if (!isResolving()) {
			setResolveInit();

			clearCandidates();

			if (fixedCandidate != null) {
				addCandidate(fixedCandidate);
			}

			setResolveEnd();
		}
	}
}
