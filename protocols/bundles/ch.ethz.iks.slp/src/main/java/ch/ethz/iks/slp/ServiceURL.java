/* Copyright (c) 2005-2007 Jan S. Rellermeyer
 * Information and Communication Systems Research Group (IKS),
 * Department of Computer Science, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of ETH Zurich nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.ethz.iks.slp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Implementation of the SLP ServiceURL class defined in RFC 2614.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zürich
 * @since 0.1
 */
public final class ServiceURL extends ch.ethz.iks.slp.impl.AuthenticatedURL
		implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9181946114021582389L;

	/**
	 * 
	 */
	public static final int NO_PORT = 0;

	/**
	 * 
	 */
	public static final int LIFETIME_NONE = 0;

	/**
	 * 
	 */
	public static final int LIFETIME_DEFAULT = 10800;

	/**
	 * 
	 */
	public static final int LIFETIME_MAXIMUM = 65535;

	/**
	 * 
	 */
	public static final int LIFETIME_PERMANENT = -1;

	/**
	 * 
	 */
	private String url = null;;

	/**
	 * 
	 */
	private int lifetime = 0;

	/**
	 * 
	 */
	private ServiceType type = null;

	/**
	 * 
	 */
	private String host = null;

	/**
	 * 
	 */
	private String protocol = null;

	/**
	 * 
	 */
	private int port = 0;

	/**
	 * 
	 */
	private String path = null;

	/**
	 * 
	 * 
	 */
	private ServiceURL() {
	}

	/**
	 * create a new ServiceURL instance from a String.
	 * 
	 * @param serviceURL
	 *            the string representation of a ServiceURL like
	 * 
	 * <pre>
	 *                 service::&quot;serviceType&quot;://&quot;addrspec&quot;
	 * </pre>
	 * 
	 * where servicetype should be of the form abstractType:concreteType and
	 * addrspec is the hostname or dotted decimal notation of the host's address
	 * followed by an optional :portNumber. Example:
	 * 
	 * <pre>
	 *                     service:osgi:remote://my.host.ch:9200
	 * </pre>
	 * 
	 * @param lifeTime
	 *            the lifetime of the ServiceURL in seconds.
	 * @throws ServiceLocationException
	 *             if the String is not parsable.
	 */
	public ServiceURL(final String serviceURL, final int lifeTime)
			throws ServiceLocationException {
		url = serviceURL;
		lifetime = lifeTime;

		try {
			parse();
		} catch (Exception ex) {
			throw new ServiceLocationException(
					ServiceLocationException.PARSE_ERROR,
					"service url is malformed: [" + url + "]. ");
		}
	}

	/**
	 * parse the url string.
	 * 
	 */
	private void parse() {
		int pos1 = url.indexOf("://");
		type = new ServiceType(url.substring(0, pos1++));

		int pos2 = url.indexOf("://", pos1 + 1);
		if (pos2 > -1) {
			protocol = url.substring(pos1 + 2, pos2);
			pos1 = pos2 + 1;
		}

		int hostEnd = url.indexOf(":", pos1 + 2);

		int pathStart;
		if (hostEnd == -1) {
			port = NO_PORT;
			pathStart = hostEnd = url.indexOf("/", pos1 + 2);
		} else {
			pathStart = url.indexOf("/", hostEnd + 1);
			if (pathStart == -1) {
				port = Integer.parseInt(url.substring(hostEnd + 1));
			} else {
				port = Integer.parseInt(url.substring(hostEnd + 1, pathStart));
			}
		}

		if (hostEnd == -1) {
			host = url.substring(pos1 + 2);
		} else {
			host = url.substring(pos1 + 2, hostEnd);
		}

		if (pathStart == -1) {
			path = "";
		} else {
			path = url.substring(pathStart);
		}
	}

	/**
	 * Check if two instances are equal.
	 * 
	 * @inheritDoc java.lang.Object.equals(Object)
	 * @param obj
	 *            the object to compare to.
	 * @return true if the instances are equal.
	 */
	public boolean equals(final Object obj) {
		if (obj instanceof ServiceURL) {
			ServiceURL u = (ServiceURL) obj;
			return (type.equals(u.type)
					&& host.equals(u.host)
					&& port == u.port
					&& ((protocol == null && u.protocol == null) || protocol
							.equals(u.protocol)) && path.equals(u.path));
		}
		return false;
	}

	/**
	 * Check if a ServiceURL matches another ServiceURL or a ServiceType. In the
	 * first case, the method performs an equality check with equals(Object
	 * obj), for ServiceTypes, the ServiceType part of the ServiceURL is checked
	 * against the given ServiceType.
	 * 
	 * @param obj
	 *            a ServiceURL or ServiceType. All other objects will return
	 *            false.
	 * @return true if the match succeeds.
	 */
	public boolean matches(final Object obj) {
		if (obj instanceof ServiceURL) {
			return equals(obj);
		} else if (obj instanceof ServiceType) {
			return type.matches(obj);
		}
		return false;
	}

	/**
	 * get a String representation of the ServiceURL.
	 * 
	 * @return the String representation.
	 */
	public String toString() {
		return type.toString() + "://"
				+ (protocol != null ? protocol + "://" : "") + host
				+ (port != NO_PORT ? (":" + port) : "") + path;
	}

	/**
	 * get the hashCode of the ServiceURL instance.
	 * 
	 * @return the hashCode.
	 */
	public int hashCode() {
		return url.hashCode();
	}

	/**
	 * get the service type.
	 * 
	 * @return the service type.
	 */
	public ServiceType getServiceType() {
		return type;
	}

	/**
	 * get the transport method.
	 * 
	 * @return the transport method. IP returns empty string.
	 * @deprecated
	 */
	public String getTransport() {
		return "";
	}

	/**
	 * get the protocol.
	 * 
	 * @return the protocol, if specified. Otherwise, returns null.
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * get the host.
	 * 
	 * @return the host.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * get the port.
	 * 
	 * @return the port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * get the URL path.
	 * 
	 * @return the URL path.
	 */
	public String getURLPath() {
		return path;
	}

	/**
	 * get the lifetime.
	 * 
	 * @return the lifetime.
	 */
	public int getLifetime() {
		return lifetime;
	}

	/**
	 * get the byte representation of the ServiceURL instance.
	 * 
	 * @throws IOException
	 * @throws IOException
	 *             if an internal processing error occurs.
	 */
	public void writeTo(DataOutputStream out) throws IOException {
		out.write(0);
		out.writeShort((short) lifetime);
		out.writeUTF(toString());
		writeAuthBlock(out);
	}

	public int getLength() {
		return 1 + 2 + 2 + toString().length() + getAuthBlockLength();
	}

	/**
	 * Reads a byte stream from a DataInput and constructs a ServiceURL from it,
	 * following the RFC 2608 schema:
	 * <p>
	 * 
	 * <pre>
	 *    0                   1                   2                   3
	 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *   |   Reserved    |          Lifetime             |   URL Length  |
	 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *   |URL len, contd.|            URL (variable length)              \
	 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *   |# of URL auths |            Auth. blocks (if any)              \
	 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * </pre>.
	 * </p>
	 * 
	 * @param input
	 *            the DataInput streaming the ServiceURL bytes.
	 * @return a ServiceURL instance.
	 * @throws ServiceLocationException
	 *             in case of IO exceptions.
	 * @throws IOException
	 */
	public static ServiceURL fromBytes(final DataInputStream input)
			throws ServiceLocationException, IOException {
		ServiceURL surl = new ServiceURL();
		input.readByte();
		surl.lifetime = input.readShort();
		surl.url = input.readUTF();
		surl.authBlocks = parseAuthBlock(input);
		surl.parse();
		return surl;
	}
}
