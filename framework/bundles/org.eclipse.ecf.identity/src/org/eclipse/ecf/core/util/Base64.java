/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

import org.eclipse.ecf.internal.core.identity.Messages;
import org.eclipse.osgi.util.NLS;

/**
 * 
 * Encode/decode byte arrays into base64 strings. Code originally acquired from
 * ftp://ftp.ora.com/pub/examples/java/crypto/files/oreilly/jonathan/util/
 * 
 * Several small modifications were made: the '_' character was substituted for
 * the '/' character, the '-' char substituted for the '=' char, and the '.'
 * substituted for the '+' char so that the resulting string does not use any of
 * the reserved characters in the reserved character set as described in
 * RFC2396. See ftp://ftp.isi.edu/in-notes/rfc2396.txt for details.
 * 
 */
public final class Base64 {
	/**
	 * Encode a byte array into a String
	 * 
	 * @param raw
	 *            the raw data to encode
	 * @return String that is base64 encoded
	 */
	public static String encode(byte[] raw) {
		if (raw == null)
			throw new NumberFormatException(Messages.Base64_Input_Data_Not_Null);
		StringBuffer encoded = new StringBuffer();
		for (int i = 0; i < raw.length; i += 3) {
			encoded.append(encodeBlock(raw, i));
		}
		return encoded.toString();
	}

	protected static char[] encodeBlock(byte[] raw, int offset) {
		int block = 0;
		int slack = raw.length - offset - 1;
		int end = (slack >= 2) ? 2 : slack;
		for (int i = 0; i <= end; i++) {
			byte b = raw[offset + i];
			int neuter = (b < 0) ? b + 256 : b;
			block += neuter << (8 * (2 - i));
		}
		char[] base64 = new char[4];
		for (int i = 0; i < 4; i++) {
			int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
			base64[i] = getChar(sixbit);
		}
		// modify to use '-' instead of '='
		if (slack < 1)
			base64[2] = '-';
		if (slack < 2)
			base64[3] = '-';
		return base64;
	}

	protected static char getChar(int sixBit) {
		if (sixBit >= 0 && sixBit <= 25)
			return (char) ('A' + sixBit);
		if (sixBit >= 26 && sixBit <= 51)
			return (char) ('a' + (sixBit - 26));
		if (sixBit >= 52 && sixBit <= 61)
			return (char) ('0' + (sixBit - 52));
		if (sixBit == 62)
			return '.';
		// modify to use '_' instead of '/'
		if (sixBit == 63)
			return '_';
		return '?';
	}

	/**
	 * Decode base64 string into a byte array.
	 * 
	 * @param base64
	 *            the base64 encoded string
	 * @return byte[] the resulting decoded data array
	 * @exception NumberFormatException
	 *                thrown if String not in base64 format.
	 */
	public static byte[] decode(String base64) throws NumberFormatException {
		int pad = 0;
		for (int i = base64.length() - 1; base64.charAt(i) == '-'; i--)
			pad++;
		int length = base64.length() * 6 / 8 - pad;
		byte[] raw = new byte[length];
		int rawIndex = 0;
		for (int i = 0; i < base64.length(); i += 4) {
			int block = (getValue(base64.charAt(i)) << 18) + (getValue(base64.charAt(i + 1)) << 12) + (getValue(base64.charAt(i + 2)) << 6) + (getValue(base64.charAt(i + 3)));
			for (int j = 0; j < 3 && rawIndex + j < raw.length; j++)
				raw[rawIndex + j] = (byte) ((block >> (8 * (2 - j))) & 0xff);
			rawIndex += 3;
		}
		return raw;
	}

	protected static int getValue(char c) throws NumberFormatException {
		if (c >= 'A' && c <= 'Z')
			return c - 'A';
		if (c >= 'a' && c <= 'z')
			return c - 'a' + 26;
		if (c >= '0' && c <= '9')
			return c - '0' + 52;
		if (c == '.')
			return 62;
		// modify to use '_' instead of '/'
		if (c == '_')
			return 63;
		// modify to use '-' instead of '='
		if (c == '-')
			return 0;
		throw new NumberFormatException(NLS.bind(Messages.Base64_Invalid_Value, String.valueOf(c)));
	}
}