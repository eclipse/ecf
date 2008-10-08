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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import ch.ethz.iks.slp.ServiceLocationException;

/**
 * an authenticated URL block within an SLPMessage.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zürich
 * @since 0.4
 */
public abstract class AuthenticatedURL {

	/**
	 * the lifetime of the authenticated URL.
	 */
	int lifetime;

	/**
	 * the auth blocks.
	 */
	protected AuthenticationBlock[] authBlocks;

	/**
	 * create a new authenticated URL.
	 */
	public AuthenticatedURL() {
		authBlocks = new AuthenticationBlock[0];
	}

	/**
	 * sign the ServiceURL.
	 * 
	 * @param spiList
	 *            the List of SPIs
	 * @throws ServiceLocationException
	 *             in case of IO errors.
	 */
	protected final void sign(final List spiList)
			throws ServiceLocationException {
		authBlocks = new AuthenticationBlock[spiList.size()];
		for (int k = 0; k < spiList.size(); k++) {
			int timestamp = SLPUtils.getTimestamp();
			timestamp += lifetime;

			String spi = (String) spiList.get(k);
			byte[] data = getAuthData(spi, timestamp);
			authBlocks[k] = new AuthenticationBlock(
					AuthenticationBlock.BSD_DSA, spi, timestamp, data, null);
		}
	}

	/**
	 * verifies the authentication blocks of the ServiceURL.
	 * 
	 * @return true if the verification succeeds.
	 * @throws ServiceLocationException
	 *             in case of IO errors.
	 */
	protected final boolean verify() throws ServiceLocationException {
		for (int i = 0; i < authBlocks.length; i++) {
			byte[] data = getAuthData(authBlocks[i].getSPI(), authBlocks[i]
					.getTimestamp());
			if (authBlocks[i].verify(data)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * get the byte representation of the authentication data.
	 * 
	 * @param spi
	 *            the SPI string as defined in RFC 2608
	 * @param timestamp
	 *            a timestamp as defined in RFC 2608
	 * @return a byte array.
	 * @throws ServiceLocationException
	 *             in case of internal errors.
	 */
	private byte[] getAuthData(final String spi, final int timestamp)
			throws ServiceLocationException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			byte[] temp = spi.getBytes();
			dos.writeShort(temp.length);
			dos.write(temp);
			temp = toString().getBytes();
			dos.writeShort(temp.length);
			dos.write(temp);
			dos.writeInt(timestamp);
			return bos.toByteArray();
		} catch (IOException ioe) {
			throw new ServiceLocationException(
					ServiceLocationException.INTERNAL_SYSTEM_ERROR, ioe
							.getMessage());
		}
	}

	/**
	 * get the authentication block bytes.
	 * 
	 * @return the bytes of the authentication block.
	 * @throws IOException
	 *             in case of IO errors.
	 */
	protected void writeAuthBlock(final DataOutputStream out)
			throws IOException {
		out.write(authBlocks.length);
		for (int i = 0; i < authBlocks.length; i++) {
			authBlocks[i].write(out);
		}
	}

	protected final int getAuthBlockLength() {
		int len = 1;
		for (int i = 0; i < authBlocks.length; i++) {
			len += authBlocks[i].getLength();
		}
		return len;
	}

	/**
	 * parse the auth blocks.
	 * 
	 * @param input
	 *            the data input.
	 * @return the auth blocks.
	 * @throws ServiceLocationException
	 *             if something goes wrong.
	 * @throws IOException
	 */
	protected static final AuthenticationBlock[] parseAuthBlock(
			final DataInputStream input) throws ServiceLocationException,
			IOException {
		return AuthenticationBlock.parse(input);
	}
}
