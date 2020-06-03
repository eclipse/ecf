/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.ui.screencapture;

import java.io.*;
import java.util.zip.*;

/**
 *
 */
public class ScreenCaptureUtil {

	public static byte[] uncompress(byte[] source) {
		final ZipInputStream ins = new ZipInputStream(new ByteArrayInputStream(source));
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int read = 0;
		final byte[] buf = new byte[16192];
		try {
			ins.getNextEntry();
			while ((read = ins.read(buf)) > 0) {
				bos.write(buf, 0, read);
			}
			bos.flush();
			ins.close();
		} catch (final IOException e) {
			// Should not happen
		}
		return bos.toByteArray();
	}

	public static byte[] compress(byte[] source) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ZipOutputStream zos = new ZipOutputStream(bos);
		final ByteArrayInputStream bis = new ByteArrayInputStream(source);
		int read = 0;
		final byte[] buf = new byte[16192];
		zos.putNextEntry(new ZipEntry("bytes")); //$NON-NLS-1$
		while ((read = bis.read(buf)) != -1) {
			zos.write(buf, 0, read);
		}
		zos.finish();
		zos.flush();
		return bos.toByteArray();
	}

}
