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
package ch.ethz.iks.slp.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import ch.ethz.iks.slp.ServiceLocationException;
import ch.ethz.iks.slp.ServiceURL;

/**
 * deregister a service from a DA.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zürich
 * @since 0.1
 */
class ServiceDeregistration extends SLPMessage {

	/**
	 * the service url.
	 */
	ServiceURL url;

	/**
	 * the scopes.
	 */
	List scopeList;

	/**
	 * the attributes.
	 */
	List attList;

	/**
	 * create a new ServiceDeregistration.
	 * 
	 * @param serviceURL
	 *            the ServiceURL.
	 * @param scopes
	 *            a List of scopes.
	 * @param attributes
	 *            the attributes.
	 * @param theLocale
	 *            the locale.
	 */
	ServiceDeregistration(final ServiceURL serviceURL, final List scopes,
			final List attributes, final Locale theLocale) {
		funcID = SRVDEREG;
		locale = theLocale;
		if (serviceURL == null) {
			throw new IllegalArgumentException("serviceURL must not be null");
		}
		url = serviceURL;
		scopeList = scopes;
		if (scopeList == null) {
			scopeList = Arrays.asList(new String[] { "default" });
		}
		attList = attributes;
		if (attList == null) {
			attList = new ArrayList();
		}
	}

	/**
	 * parse a ServiceDeregistration from an input stream.
	 * 
	 * @param input
	 *            the stream.
	 * @throws ServiceLocationException
	 *             if something goes wrong.
	 */
	public ServiceDeregistration(final DataInputStream input)
			throws ServiceLocationException, IOException {
		scopeList = stringToList(input.readUTF(), ",");
		url = ServiceURL.fromBytes(input);
		attList = stringToList(input.readUTF(), ",");

		if (SLPCore.CONFIG.getSecurityEnabled()) {
			if (!verify()) {
				throw new ServiceLocationException(
						ServiceLocationException.AUTHENTICATION_FAILED,
						"Authentication failed for " + toString());
			}
		}
	}

	/**
	 * get the bytes from a ServiceDeregistration:
	 * <p>
	 * 
	 * <pre>
	 *          0                   1                   2                   3
	 *          0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *         |         Service Location header (function = SrvDeReg = 4)     |
	 *         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *         |    Length of &lt;scope-list&gt;     |         &lt;scope-list&gt;          \
	 *         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *         |                           URL Entry                           \
	 *         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *         |      Length of &lt;tag-list&gt;     |            &lt;tag-list&gt;         \
	 *         +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * </pre>.
	 * </p>
	 * 
	 * @return the bytes.
	 * @throws ServiceLocationException
	 *             in case of IO errors.
	 */
	protected void writeTo(final DataOutputStream out) throws IOException {
		super.writeHeader(out, getSize());
		out.writeUTF(listToString(scopeList, ","));
		url.writeTo(out);
		out.writeUTF(listToString(attList, ","));
	}

	/**
	 * get the length of the message.
	 * 
	 * @return the length of the message.
	 * @see ch.ethz.iks.slp.impl.SLPMessage#getSize()
	 */
	int getSize() {
		return getHeaderSize() + 2 + listToString(scopeList, ",").length()
				+ url.getLength() + 2
				+ listToString(attList, ",").length();
	}

	/**
	 * sign this ServiceDeregistration.
	 * 
	 * @param spiList
	 *            a List of SPIs.
	 * @throws ServiceLocationException
	 *             in case of IO errors.
	 */
	void sign(final List spiList) throws ServiceLocationException {
		url.sign(spiList);
	}

	/**
	 * verify the ServiceDeregistration.
	 * 
	 * @return true if it could be verified.
	 * @throws ServiceLocationException
	 *             in case of IO errors.
	 */
	boolean verify() throws ServiceLocationException {
		if (!url.verify()) {
			return false;
		}
		return true;
	}

}
