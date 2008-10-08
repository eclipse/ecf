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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.iks.slp.ServiceLocationException;

/**
 * Implementation of the SLP Authentication Block.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 * @since 0.4
 */
class AuthenticationBlock {

	/**
	 * the BSD code for DSA.
	 */
	public static final short BSD_DSA = 0x0002;

	/**
	 * the timestamp.
	 */
	private int timestamp;

	/**
	 * the data.
	 */
	private byte[] data = null;

	/**
	 * the signature.
	 */
	private byte[] sig = null;

	/**
	 * the SPI.
	 */
	private String spi = null;

	/**
	 * create a new Instance of an AuthenticationBlock.
	 * 
	 * @param bsd
	 *            the BSD, only BSD_DSA is currently supported.
	 * @param spiStr
	 *            the SPI String.
	 * @param timeStamp
	 *            the timestamp.
	 * @param byteData
	 *            the binary data.
	 * @param signature
	 *            the signature, if avaliable.
	 * @throws ServiceLocationException
	 *             in case of processing errors.
	 */
	AuthenticationBlock(final short bsd, final String spiStr,
			final int timeStamp, final byte[] byteData, final byte[] signature)
			throws ServiceLocationException {
		this();

		if (bsd != 0x0002) {
			throw new ServiceLocationException(
					ServiceLocationException.NOT_IMPLEMENTED,
					"Only BSD 0x0002 (DSA) is supported.");
		}

		timestamp = timeStamp;
		data = byteData;
		spi = spiStr;

		if (signature == null) {
			sign();
		} else {
			sig = signature;
		}
	}

	/**
	 * 
	 * 
	 */
	AuthenticationBlock() {
	}

	/**
	 * sign the AuthenticationBlock.
	 * 
	 * @throws ServiceLocationException
	 *             in case of processing errors.
	 */
	private void sign() throws ServiceLocationException {
		try {
			PrivateKey privateKey = SLPCore.CONFIG.getPrivateKey(spi);
			SLPCore.platform.logDebug("Signing with SPI: " + spi);
			Signature signature = Signature.getInstance("SHA1withDSA");
			signature.initSign(privateKey);
			signature.update(data);
			sig = signature.sign();
		} catch (Exception e) {
			SLPCore.platform.logError(e.getMessage(), e.fillInStackTrace());
			throw new ServiceLocationException(
					ServiceLocationException.AUTHENTICATION_FAILED,
					"Could not sign data");
		}
	}

	/**
	 * get the SPI.
	 * 
	 * @return the SPI.
	 */
	String getSPI() {
		return spi;
	}

	/**
	 * get the timestamp.
	 * 
	 * @return the timestamp.
	 */
	int getTimestamp() {
		return timestamp;
	}

	/**
	 * verify the authBlock.
	 * 
	 * @param verData
	 *            the auth data.
	 * @return true if verification suceeds.
	 * @throws ServiceLocationException
	 *             in case of IO errors.
	 */
	boolean verify(final byte[] verData) throws ServiceLocationException {
		try {
			PublicKey publicKey = SLPCore.CONFIG.getPublicKey(spi);

			Signature signature = Signature.getInstance("SHA1withDSA");
			signature.initVerify(publicKey);
			signature.update(verData);
			boolean success = signature.verify(sig);
			SLPCore.platform.logDebug((success ? "Verified with SPI: "
						: "Verification failed with SPI: ")
						+ spi);

			return success;
		} catch (Exception e) {
			SLPCore.platform.logError(e.getMessage(), e.fillInStackTrace());
			throw new ServiceLocationException(
					ServiceLocationException.AUTHENTICATION_FAILED,
					"Could not verify data with SPI: " + spi);
		}
	}

	/**
	 * calculates the length of this auth block.
	 * 
	 * @return the length.
	 */
	int getLength() {
		return 2 // BSD
				+ 2 // Block length
				+ 4 // timestamp
				+ 2 // spi length
				+ spi.getBytes().length // spi
				+ sig.length; // signature
	}

	/**
	 * get the bytes.
	 * 
	 * @return the bytes.
	 * @throws IOException
	 *             in case of IO errors.
	 */
	void write(final DataOutputStream out) throws IOException {
		out.writeShort(BSD_DSA); // BSD
		out.writeShort((short) getLength());
		out.writeInt(timestamp);
		byte[] temp = spi.getBytes();
		out.writeShort(temp.length);
		out.write(temp);
		out.write(sig);
	}

	/**
	 * parse a AuthenticationBlock array.
	 * 
	 * @param input
	 *            the DataInput.
	 * @return a AuthenticationBlock array.
	 * @throws ServiceLocationException
	 *             in case of parse / IO errors.
	 * @throws IOException
	 * @throws ServiceLocationException
	 */
	static AuthenticationBlock[] parse(final DataInputStream input)
			throws IOException, ServiceLocationException {

		List blocks = new ArrayList();
		short blockCount = (short) input.readByte();
		for (int i = 0; i < blockCount; i++) {
			AuthenticationBlock authBlock = new AuthenticationBlock();
			short bsd = (short) input.readShort();
			if (bsd != BSD_DSA) {
				throw new ServiceLocationException(
						ServiceLocationException.AUTHENTICATION_FAILED, "BSD "
								+ bsd + " is not supported.");
			}
			int size = input.readShort();
			authBlock.timestamp = input.readInt();
			authBlock.spi = input.readUTF();
			authBlock.sig = new byte[size - 2 - 2 - 4 - 2
					- authBlock.spi.getBytes().length];
			try {
				input.readFully(authBlock.sig);
			} catch (IOException ioe) {
				throw new ServiceLocationException(
						ServiceLocationException.PARSE_ERROR, ioe.getMessage());
			}
			blocks.add(authBlock);
		}

		if (!SLPCore.CONFIG.getSecurityEnabled()) {
			return new AuthenticationBlock[0];
		}

		return (AuthenticationBlock[]) blocks
				.toArray(new AuthenticationBlock[0]);
	}
}
