/*******************************************************************************
 * Copyright (c) 2004, 2007 Remy Suen, Composent, Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.example.collab.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

public class ImageWrapper implements Serializable {

	private static final long serialVersionUID = -834839369167998998L;

	public final int width;
	public final int height;
	public final int depth;
	public final int scanlinePad;
	public byte[] data;

	private final int redMask;
	private final int greenMask;
	private final int blueMask;

	ImageWrapper(ImageData data) {
		width = data.width;
		height = data.height;
		depth = data.depth;
		scanlinePad = data.scanlinePad;
		this.data = null;
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ZipOutputStream zos = new ZipOutputStream(bos);
		final ZipEntry entry = new ZipEntry("bytes");
		final ByteArrayInputStream bis = new ByteArrayInputStream(data.data);
		int read = 0;
		final byte[] buf = new byte[16384];
		try {
			zos.putNextEntry(entry);
			while ((read = bis.read(buf)) != -1) {
				zos.write(buf, 0, read);
			}
			zos.finish();
			zos.flush();
		} catch (final IOException e) {
			// Should never happen
		}
		this.data = bos.toByteArray();
		redMask = data.palette.redMask;
		greenMask = data.palette.greenMask;
		blueMask = data.palette.blueMask;
	}

	protected byte[] getUncompressedData() throws IOException {
		final ZipInputStream ins = new ZipInputStream(new ByteArrayInputStream(this.data));
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int read = 0;
		final byte[] buf = new byte[16384];
		ins.getNextEntry();
		while ((read = ins.read(buf)) > 0) {
			bos.write(buf, 0, read);
		}
		bos.flush();
		ins.close();
		return bos.toByteArray();
	}

	public ImageData createImageData() throws IOException {
		final PaletteData palette = new PaletteData(redMask, greenMask, blueMask);
		return new ImageData(width, height, depth, palette, scanlinePad, getUncompressedData());
	}
}
