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
import ch.ethz.iks.slp.ServiceType;
import ch.ethz.iks.slp.ServiceURL;

/**
 * a AttributeRequest Message is sent to discover the attributes of a service.
 * 
 * @author Jan S. Rellermeyer
 * @since 0.1
 */
class AttributeRequest extends RequestMessage {
	/**
	 * the url of the service.
	 */
	String url;

	/**
	 * a list of tags that are requested if they exist.
	 */
	List tagList;

	/**
	 * the spi string.
	 */
	String spi;

	/**
	 * create an AttributeRequest message for a ServiceURL.
	 * 
	 * @param serviceURL
	 *            the ServiceURL
	 * @param scopes
	 *            a list of scopes that are included.
	 * @param tags
	 *            a list of tags that are requested. If omitted, all attribute
	 *            values will be returned.
	 * @param theLocale
	 *            the Locale of the message.
	 */
	AttributeRequest(final ServiceURL serviceURL, final List scopes,
			final List tags, final Locale theLocale) {
		funcID = ATTRRQST;
		url = serviceURL.toString();
		scopeList = scopes;
		if (scopeList == null) {
			scopeList = new ArrayList();
			scopeList.add("default");
		}
		tagList = tags;
		if (tagList == null) {
			tagList = new ArrayList();
		}
		locale = theLocale == null ? SLPCore.DEFAULT_LOCALE : theLocale;
		spi = SLPCore.CONFIG.getSecurityEnabled() ? SLPCore.CONFIG.getSPI()
				: "";
	}

	/**
	 * create an AttributeRequest message for a ServiceType.
	 * 
	 * @param type
	 *            the ServiceType.
	 * @param scopes
	 *            a list of scopes that are included.
	 * @param tags
	 *            a list of tags that are requested. If omitted, all attribute
	 *            values will be returned.
	 * @param theLocale
	 *            the Locale of the message.
	 */
	AttributeRequest(final ServiceType type, final List scopes,
			final List tags, final Locale theLocale) {
		funcID = ATTRRQST;
		url = type.toString();
		scopeList = scopes;
		if (scopeList == null) {
			scopeList = new ArrayList();
			scopeList.add("default");
		}
		tagList = tags;
		if (tagList == null) {
			tagList = new ArrayList();
		}
		locale = theLocale == null ? SLPCore.DEFAULT_LOCALE : theLocale;
		spi = SLPCore.CONFIG.getSecurityEnabled() ? SLPCore.CONFIG.getSPI()
				: "";
	}

	/**
	 * create a new AttributeRequest from a DataInput streaming the bytes of an
	 * AttributeReply message body.
	 * 
	 * @param input
	 *            stream of bytes forming the message body.
	 * @throws ServiceLocationException
	 *             if IO Exceptions occure.
	 */
	AttributeRequest(final DataInputStream input) throws IOException {
		prevRespList = stringToList(input.readUTF(), ",");
		url = input.readUTF();
		scopeList = stringToList(input.readUTF(), ",");
		tagList = stringToList(input.readUTF(), ",");
		spi = input.readUTF();
	}

	/**
	 * get the bytes of the message body in the following RFC 2608 compliant
	 * format:
	 * <p>
	 * 
	 * <pre>
	 *         0                   1                   2                   3
	 *         0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *        |       Service Location header (function = AttrRqst = 6)       |
	 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *        |       length of PRList        |        &lt;PRList&gt; String        \
	 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *        |         length of URL         |              URL              \
	 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *        |    length of &lt;scope-list&gt;     |      &lt;scope-list&gt; string      \
	 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *        |  length of &lt;tag-list&gt; string  |       &lt;tag-list&gt; string       \
	 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *        |   length of &lt;SLP SPI&gt; string  |        &lt;SLP SPI&gt; string       \
	 *        +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * </pre>.
	 * </p>
	 * 
	 * @return array of bytes.
	 * @throws ServiceLocationException
	 * @throws ServiceLocationException
	 *             if an IO Exception occurs.
	 */
	protected void writeTo(final DataOutputStream out) throws IOException {
		super.writeHeader(out, getSize());
		out.writeUTF(listToString(prevRespList, ","));
		out.writeUTF(url);
		out.writeUTF(listToString(scopeList, ","));
		out.writeUTF(listToString(tagList, ","));
		out.writeUTF(spi);
	}

	/**
	 * get the length of the message.
	 * 
	 * @return the length of the message.
	 * @see ch.ethz.iks.slp.impl.SLPMessage#getSize()
	 */
	int getSize() {
		return getHeaderSize() + 2 + listToString(prevRespList, ",").length()
				+ 2 + url.length() + 2 + listToString(scopeList, ",").length()
				+ 2 + listToString(tagList, ",").length() + 2 + spi.length();
	}

	/**
	 * get a string representation of the AttributeReply message.
	 * 
	 * @return a String displaying the properties of this message instance.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		buffer.append(", prevRespList: " + prevRespList);
		buffer.append(", URL: " + url);
		buffer.append(", scopeList: " + scopeList);
		buffer.append(", tag-list: " + tagList);
		buffer.append(", slpSpi: " + spi);
		return buffer.toString();
	}
}
