package org.jivesoftware.smackx.jingle;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.nat.BasicResolver;
import org.jivesoftware.smackx.nat.FixedResolver;
import org.jivesoftware.smackx.nat.STUNResolver;
import org.jivesoftware.smackx.nat.TransportResolver;
import org.jivesoftware.smackx.packet.JingleTransport;

/**
 * Transport manager for Jingle.
 * 
 * This class makes easier the use of transport resolvers by presenting a simple
 * interface for algorithm selection. The transport manager also keeps the match
 * between the resolution method and the &lt;transport&gt; element present in
 * Jingle packets.
 * 
 * This class must be used with a JingleManager instance in the following way:
 * 
 * <pre>
 * TransportManager tm = new TransportManager();
 * tm.useSTUN(); // or some other method...
 * 
 * JingleManager jm = new JingleManager(connection, tm.getResolver());
 * jm.createOutgoingJingleSession(responder, payloads);
 * </pre>
 * 
 * @author Alvaro Saurin <alvaro.saurin@gmail.com>
 */
public class TransportManager {
	// This class implements the context of a Strategy pattern...

	// Current resolver.
	private TransportResolver resolver;

	// An instance of the transport resolver. This doesn't need to match
	// the resolver, as we can use a STUNResolver for the JingleTransport.Ice or
	// JingleTransport.RawUdp resolvers...
	private JingleTransport jingleTransport;

	/**
	 * Deafult contructor.
	 */
	public TransportManager() {
		useSTUNResolver(); // We use Raw-UDP/STUN by default...
	}

	/**
	 * Contructor with an external resolver.
	 */
	public TransportManager(final TransportResolver resol, final JingleTransport trans) {
		resolver = resol;
		jingleTransport = trans;
	}

	/**
	 * Use the simple resolver.
	 */
	public void useSimpleResolver() {
		if (resolver != null && !(resolver instanceof BasicResolver)) {
			try {
				resolver.cancel();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		resolver = new BasicResolver();

		if (jingleTransport == null
				|| !(jingleTransport instanceof JingleTransport.RawUdp)) {
			jingleTransport = new JingleTransport.RawUdp();
		}
	}

	/**
	 * Use a simple resolver, with a fixed IP address and port.
	 * 
	 * @param ip the IP address
	 * @param port the port to use (0 for any port)
	 */
	public void useSimpleResolver(final String ip, final int port) {
		if (resolver != null && !(resolver instanceof FixedResolver)) {
			try {
				resolver.cancel();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		resolver = new FixedResolver(ip, port);

		if (jingleTransport == null
				|| !(jingleTransport instanceof JingleTransport.RawUdp)) {
			jingleTransport = new JingleTransport.RawUdp();
		}
	}

	/**
	 * Returns true if the transport manager is using the simple resolver
	 * 
	 * @return true if the transport manager is using the simple resolver
	 */
	public boolean isUsingSimpleResolver() {
		return (resolver instanceof BasicResolver || resolver instanceof FixedResolver)
				&& jingleTransport instanceof JingleTransport.RawUdp;
	}

	/**
	 * Use the STUN resolver.
	 */
	public void useSTUNResolver() {
		if (resolver != null && !(resolver instanceof STUNResolver)) {
			try {
				resolver.cancel();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		resolver = new STUNResolver();

		if (jingleTransport == null
				|| !(jingleTransport instanceof JingleTransport.RawUdp)) {
			jingleTransport = new JingleTransport.RawUdp();
		}
	}

	/**
	 * Returns true if the transport manager is using the STUN resolver
	 * 
	 * @return true if the transport manager is using the STUN resolver
	 */
	public boolean isUsingSTUNResolver() {
		return resolver instanceof STUNResolver
				&& jingleTransport instanceof JingleTransport.RawUdp;
	}

	/**
	 * Use the ICE resolver.
	 */
	public void useICEResolver() {
		// Not implemented yet
		resolver = null;
		jingleTransport = new JingleTransport.Ice();
	}

	/**
	 * Returns true if the transport manager is using the ICE resolver
	 * 
	 * @return true if the transport manager is using the ICE resolver
	 */
	public boolean isUsingICEResolver() {
		return false;
	}

	/**
	 * Obtain the JingleTransport for the current resolver
	 * 
	 * @return a JingleTransport instance
	 */
	public JingleTransport getJingleTransport() {
		return jingleTransport;
	}

	/**
	 * Obtain the resolver
	 * 
	 * @return a TransportResolver.
	 */
	public TransportResolver getResolver() {
		return resolver;
	}
}
