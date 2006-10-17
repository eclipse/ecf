package org.jivesoftware.smackx.nat;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.jivesoftware.smack.XMPPException;

/**
 * Simple resolver.
 */
public class BasicResolver extends TransportResolver {

	/**
	 * Constructor.
	 */
	public BasicResolver() {
		super();
	}

	/**
	 * Resolve the IP address.
	 * 
	 * The BasicResolver takes the IP addresses of the interfaces and uses the
	 * first non-loopback address.
	 */
	public synchronized void resolve() throws XMPPException {

		setResolveInit();

		clearCandidates();

		Enumeration ifaces = null;

		try {
			ifaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		while (ifaces.hasMoreElements()) {

			NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
			Enumeration iaddresses = iface.getInetAddresses();

			while (iaddresses.hasMoreElements()) {
				InetAddress iaddress = (InetAddress) iaddresses.nextElement();
				if (!iaddress.isLoopbackAddress() && !iaddress.isLinkLocalAddress()) {
					addCandidate(new TransportCandidate.Fixed(iaddress.getHostName(), 0));
				}
			}
		}

		setResolveEnd();
	}

	public void cancel() throws XMPPException {
		// Nothing to do here
	}
}
