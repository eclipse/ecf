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
import java.util.List;
import ch.ethz.iks.slp.ServiceLocationException;

/**
 * a ServiceReply Message is sent as reaction of a ServiceRequest.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zürich
 * @since 0.6
 */
class ServiceTypeReply extends ReplyMessage {
	/**
	 * a List of ServiceURLs.
	 */
	List serviceTypes;

	/**
	 * create a new ServiceTypeReply from a list of ServiceTypes.
	 * 
	 * @param req
	 *            the request to reply to.
	 * @param types
	 *            the ServiceTypes.
	 */
	ServiceTypeReply(final ServiceTypeRequest req, final List types) {
		this.funcID = SRVTYPERPLY;
		this.locale = req.locale;
		this.xid = req.xid;
		this.address = req.address;
		this.port = req.port;
		this.errorCode = 0;
		this.serviceTypes = types;
	}

	/**
	 * create a new ServiceTypeReply from a DataInput streaming the bytes of an
	 * ServiceTypeReply message body.
	 * 
	 * @param input
	 *            stream of bytes forming the message body.
	 * @throws ServiceLocationException
	 *             in case that the IO caused an exception.
	 */
	ServiceTypeReply(final DataInputStream input) throws IOException {
		errorCode = input.readShort();
		serviceTypes = stringToList(input.readUTF(), ",");
	}

	/**
	 * get the bytes of the message body in the following RFC 2608 compliant
	 * format:
	 * <p>
	 * 
	 * <pre>
	 *    0                   1                   2                   3
	 *    0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *   |      Service Location header (function = SrvTypeRply = 10)    |
	 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *   |           Error Code          |    length of &lt;srvType-list&gt;   |
	 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *   |                       &lt;srvtype--list&gt;                         \
	 *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 * </pre>.
	 * </p>
	 * 
	 * @return array of bytes.
	 * @throws ServiceLocationException
	 *             if an IO Exception occurs.
	 */
	protected void writeTo(final DataOutputStream out) throws IOException {
		super.writeHeader(out, getSize());
		out.writeShort(errorCode);
		out.writeUTF(listToString(serviceTypes, ","));
	}

	/**
	 * get the length of the message.
	 * 
	 * @return the length of the message.
	 * @see ch.ethz.iks.slp.impl.SLPMessage#getSize()
	 */
	int getSize() {
		return getHeaderSize() + 2 + 2
				+ listToString(serviceTypes, ",").length();
	}

	/**
	 * get the result of the reply message.
	 * 
	 * @return the <code>List</code> of results.
	 * @see ch.ethz.iks.slp.impl.ReplyMessage#getResult()
	 */
	List getResult() {
		return serviceTypes;
	}

	/**
	 * get a string representation of the ServiceTypeReply message.
	 * 
	 * @return a String displaying the properties of this message instance.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(super.toString());
		buffer.append(", errorCode " + errorCode);
		buffer.append(", ServiceTypeCount " + serviceTypes.size());
		buffer.append(", ServiceTypes " + serviceTypes);
		return buffer.toString();
	}
}
