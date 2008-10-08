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
import java.util.List;
import java.util.Locale;

import ch.ethz.iks.slp.ServiceLocationException;

/**
 * ServiceTypeRequest message is used to find existing service types.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zürich
 * @since 0.6
 */
class ServiceTypeRequest extends RequestMessage {

	/**
	 * the naming authority.
	 */
	String namingAuthority;
	
	private static final String NA_ALL = "*";
	private static final String NA_DEFAULT = "";

	/**
	 * creates a new ServiceTypeRequest.
	 * 
	 * @param authority
	 *            the naming authority.
	 * @param scopes
	 *            a list of scopes to be included.
	 * @param theLocale
	 *            the Locale of the message.
	 */
	ServiceTypeRequest(final String authority, final List scopes,
			final Locale theLocale) {
		funcID = SRVTYPERQST;
		prevRespList = new ArrayList();
		namingAuthority = authority != null ? authority : NA_ALL;
		scopeList = scopes;
		if (scopeList == null) {
			scopeList = new ArrayList();
			scopeList.add("default");
		}

		locale = theLocale == null ? SLPCore.DEFAULT_LOCALE : theLocale;
	}

	/**
	 * create a new ServiceTypeRequest from a DataInput streaming the bytes of a
	 * ServiceTypeRequest message body.
	 * 
	 * @param input
	 *            stream of bytes forming the message body.
	 * @throws ServiceLocationException
	 *             in case that the IO caused an exception.
	 */
	ServiceTypeRequest(final DataInputStream input) throws IOException {
		prevRespList = stringToList(input.readUTF(), ",");
		final int authLen = input.readUnsignedShort();
		if (authLen == 0xFFFF) {
			namingAuthority = NA_ALL;
		}else if(authLen == -1) {
			namingAuthority = NA_DEFAULT;
		} else {
			byte[] buf = new byte[authLen];
			input.readFully(buf);
			namingAuthority = new String(buf);
		}
		scopeList = stringToList(input.readUTF(), ",");
	}

	/**
	 * get the bytes of the message body in the following RFC 2608 compliant
	 * format:
	 * <p>
	 * 
	 * <pre>
	 *   0                   1                   2                   3
	 *   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *  |      Service Location header (function = SrvTypeRqst = 9)     |
	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *  |        length of PRList       |        &lt;PRList&gt; String        \
	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *  |   length of Naming Authority  |   &lt;Naming Authority String&gt;   \
	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *  |     length of &lt;scope-list&gt;    |      &lt;scope-list&gt; String      \
	 *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * </pre>.
	 * </p>
	 * 
	 * @return array of bytes.
	 * @throws IOException
	 * @throws ServiceLocationException
	 * @throws ServiceLocationException
	 *             if an IO Exception occurs.
	 */
	protected void writeTo(final DataOutputStream out) throws IOException {
		super.writeHeader(out, getSize());
		out.writeUTF(listToString(prevRespList, ","));
		if (namingAuthority.equals(NA_ALL)) {
			out.writeShort(0xFFFF);
		} else if (namingAuthority.equals(NA_DEFAULT)) {
			out.writeUTF("");
		} else {
			out.writeUTF(namingAuthority);
		}
		out.writeUTF(listToString(scopeList, ","));
	}

	/**
	 * get the length of the message.
	 * 
	 * @return the length of the message.
	 * @see ch.ethz.iks.slp.impl.SLPMessage#getSize()
	 */
	int getSize() {
		int len = getHeaderSize() + 2
				+ listToString(prevRespList, ",").length();
		if(namingAuthority.equals(NA_DEFAULT) || namingAuthority.equals(NA_ALL)) {
			len += 2;
		} else {
			len += 2 + namingAuthority.length();
		}
		len += 2 + listToString(scopeList, ",").length();
		return len;
	}

	/**
	 * get a string representation of the ServiceTypeRequest message.
	 * 
	 * @return a String displaying the properties of this message instance.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		buffer.append(", prevRespList: " + prevRespList);
		if(namingAuthority.equals(NA_ALL)) {
			buffer.append(", namingAuthority: ALL (NA_ALL)");
		} else if (namingAuthority.equals(NA_DEFAULT)) {
			buffer.append(", namingAuthority: IANA (NA_DEFAULT)");
		} else {
			buffer.append(", namingAuthority: " + namingAuthority);
		}
		buffer.append(", scopeList: " + scopeList);
		return buffer.toString();
	}
}
