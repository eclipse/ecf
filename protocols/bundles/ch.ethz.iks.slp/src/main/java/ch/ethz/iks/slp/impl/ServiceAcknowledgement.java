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

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import ch.ethz.iks.slp.ServiceLocationException;

/**
 * a ServiceAcknowledgement is sent by a DA as reaction to a ServiceRegistration
 * or ServiceDeregistration.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zürich
 * @since 0.1
 */
class ServiceAcknowledgement extends ReplyMessage {
	/**
	 * create a new ServiceAcknowledgement from a DataInput streaming the bytes
	 * of an ServiceAcknowledgement message body.
	 * 
	 * @param input
	 *            stream of bytes forming the message body.
	 * @throws ServiceLocationException
	 *             in case that the IO caused an exception.
	 * @throws IOException
	 */
	ServiceAcknowledgement(final DataInput input) throws IOException {
		errorCode = input.readShort();
	}

	/**
	 * create a new ServiceAcknowledgement.
	 * 
	 * @param msg
	 *            the SLPMessage to acknowledge.
	 * @param error
	 *            the error code.
	 */
	ServiceAcknowledgement(final SLPMessage msg, final int error) {
		funcID = SRVACK;
		xid = msg.xid;
		locale = msg.locale;
		address = msg.address;
		port = msg.port;
		errorCode = error;
	}

	/**
	 * get the bytes of the message body in the following RFC 2608 compliant
	 * format:
	 * <p>
	 * 
	 * <pre>
	 *       0                   1                   2                   3
	 *       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *      |          Service Location header (function = SrvAck = 5)      |
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	 *      |          Error Code           |
	 *      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
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
	}

	/**
	 * get the length of the message.
	 * 
	 * @return the length of the message.
	 * @see ch.ethz.iks.slp.impl.SLPMessage#getSize()
	 */
	int getSize() {
		return getHeaderSize() + 2;
	}

	/**
	 * get the result.
	 * 
	 * @see ch.ethz.iks.slp.impl.ReplyMessage#getResult()
	 * @return the <code>List</code> of results.
	 */
	List getResult() {
		return null;
	}

	/**
	 * get a string representation of the ServiceAcknowledgement message.
	 * 
	 * @return a String displaying the properties of this message instance.
	 */
	public String toString() {
		return super.toString() + ", errorCode " + errorCode;
	}
}
