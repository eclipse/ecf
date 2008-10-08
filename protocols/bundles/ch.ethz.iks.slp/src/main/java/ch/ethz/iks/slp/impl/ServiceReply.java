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
import java.util.Iterator;
import java.util.List;
import ch.ethz.iks.slp.ServiceLocationException;
import ch.ethz.iks.slp.ServiceURL;

/**
 * a ServiceReply Message is sent as reaction to a ServiceRequest.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zürich
 * @since 0.1
 */
class ServiceReply extends ReplyMessage {
	/**
	 * a List of ServiceURLs.
	 */
	List urlList;

	/**
	 * create a new ServiceReply from a list of ServiceURLs.
	 * 
	 * @param req
	 *            the ServiceRequest to reply to.
	 * @param urls
	 *            the result URLs.
	 */
	ServiceReply(final ServiceRequest req, final List urls) {
		this.funcID = SRVRPLY;
		this.xid = req.xid;
		this.locale = req.locale;
		this.address = req.address;
		this.port = req.port;
		this.errorCode = 0;
		this.urlList = urls;
	}

	/**
	 * create a new ServiceReply from a DataInput streaming the bytes of an
	 * ServiceReply message body.
	 * 
	 * @param input
	 *            stream of bytes forming the message body.
	 * @throws ServiceLocationException
	 *             in case that the IO caused an exception.
	 * @throws IOException
	 */
	ServiceReply(final DataInputStream input) throws ServiceLocationException,
			IOException {
		errorCode = input.readShort();
		short entryCount = input.readShort();
		urlList = new ArrayList();

		for (int i = 0; i < entryCount; i++) {
			urlList.add(ServiceURL.fromBytes(input));
		}
		if (SLPCore.CONFIG.getSecurityEnabled()) {
			if (!verify())
				throw new ServiceLocationException(
						ServiceLocationException.AUTHENTICATION_FAILED,
						toString());
		}
	}

	/**
	 * get the bytes of the message body in the following RFC 2608 compliant
	 * format:
	 * <p>
	 * 
	 * <pre>
	 *      0                   1                   2                   3
	 *      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *     |        Service Location header (function = SrvRply = 2)       |
	 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *     |        Error Code             |        URL Entry count        |
	 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *     |       &lt;URL Entry 1&gt;          ...       &lt;URL Entry N&gt;          \
	 *     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * 
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @return array of bytes.
	 * @throws ServiceLocationException
	 *             if an IO Exception occurs.
	 */
	protected void writeTo(final DataOutputStream out) throws IOException {
		super.writeHeader(out, getSize());
		out.writeShort(errorCode);
		out.writeShort(urlList.size());
		for (int i = 0; i < urlList.size(); i++) {
			((ServiceURL) urlList.get(i)).writeTo(out);
		}
	}

	/**
	 * get the length of the message.
	 * 
	 * @return the length of the message.
	 * @see ch.ethz.iks.slp.impl.SLPMessage#getSize()
	 */
	int getSize() {
		int len = getHeaderSize() + 2 + 2;
		for (int i = 0; i < urlList.size(); i++) {
			len += ((ServiceURL) urlList.get(i)).getLength();
		}
		return len;
	}

	List getResult() {
		return urlList;
	}

	/**
	 * get a string representation of the AttributeReply message.
	 * 
	 * @return a String displaying the properties of this message instance.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		buffer.append(", errorCode " + errorCode);
		buffer.append(", URLCount " + urlList.size());
		buffer.append(", URLs " + urlList);
		return buffer.toString();
	}

	/**
	 * sign the ServiceReply.
	 * 
	 * @param spiStr
	 *            the SPI String.
	 * @throws ServiceLocationException
	 *             in case of IO errors.
	 */
	void sign(final String spiStr) throws ServiceLocationException {
		List spiList = stringToList(spiStr, ",");
		for (Iterator urlIter = urlList.iterator(); urlIter.hasNext();) {
			ServiceURL url = (ServiceURL) urlIter.next();
			url.sign(spiList);
		}
	}

	/**
	 * verify the ServiceReply.
	 * 
	 * @return true if it could be verified.
	 * @throws ServiceLocationException
	 *             in case of IO errors.
	 */
	boolean verify() throws ServiceLocationException {
		for (Iterator urlIter = urlList.iterator(); urlIter.hasNext();) {
			ServiceURL url = (ServiceURL) urlIter.next();
			if (!url.verify()) {
				return false;
			}
		}
		return true;
	}
}
