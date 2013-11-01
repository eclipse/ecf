/* Copyright (c) 2006-2011 Jan S. Rellermeyer
 * Systems Group, ETH Zurich.
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

package ch.ethz.iks.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import ch.ethz.iks.r_osgi.types.BoxedPrimitive;

/**
 * Smart object output stream that is able to deserialize classes which do not
 * implement Serializable. It only rejects classes which have native code parts
 * and the OSGi ServiceReference and ServiceRegistration classes.
 * 
 * @author Jan S. Rellermeyer
 */
public final class SmartObjectOutputStream extends ObjectOutputStream {

	static Set blackList = new HashSet();
	static {
		blackList.add("org.osgi.framework.ServiceReference"); //$NON-NLS-1$
		blackList.add("org.osgi.framework.ServiceRegistration"); //$NON-NLS-1$
	}

	public SmartObjectOutputStream(final OutputStream out) throws IOException {
		super(new EnhancedGZIPOutputStream(out));
		this.enableReplaceObject(true);
	}

	protected Object replaceObject(final Object obj) throws IOException {
		if (obj instanceof BoxedPrimitive) {
			return ((BoxedPrimitive) obj).getBoxed();
		}

		if (obj instanceof Serializable) {
			return obj;
		}

		final Class clazz = obj.getClass();
		if (blackList.contains(clazz.getName())) {
			throw new NotSerializableException(clazz.getName());
		}

		return new SmartObjectStreamClass(obj, clazz);
	}

	static class EnhancedGZIPOutputStream extends GZIPOutputStream {

		private static final byte[] NOTHING = new byte[0];
		private boolean hasPendingBytes = false;

		public EnhancedGZIPOutputStream(final OutputStream out)
				throws IOException {
			super(out);
			def.setLevel(Deflater.BEST_SPEED);
		}

		public void write(final byte[] bytes, final int i, final int i1)
				throws IOException {
			super.write(bytes, i, i1);
			hasPendingBytes = true;
		}

		protected void deflate() throws IOException {
			int len;
			do {
				len = def.deflate(buf, 0, buf.length);
				if (len == 0) {
					break;
				}
				this.out.write(buf, 0, len);
			} while (true);
		}

		public void flush() throws IOException {
			if (!hasPendingBytes) {
				return;
			}

			if (!def.finished()) {
				def.setInput(NOTHING, 0, 0);
				def.setLevel(Deflater.NO_COMPRESSION);
				deflate();
				def.setLevel(Deflater.BEST_SPEED);
				deflate();
				super.flush();
			}

			hasPendingBytes = false;
		}
	}
}
