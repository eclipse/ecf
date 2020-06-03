/****************************************************************************
 * Copyright (c) 2004, 2007 Remy Suen, Composent, Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.ui.screencapture;

import java.io.Serializable;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

public class ImageWrapper implements Serializable {

	private static final long serialVersionUID = -834839369167998998L;

	public final int width;
	public final int height;
	public final int depth;
	public final int scanlinePad;

	private final int redMask;
	private final int greenMask;
	private final int blueMask;

	public ImageWrapper(ImageData data) {
		width = data.width;
		height = data.height;
		depth = data.depth;
		scanlinePad = data.scanlinePad;
		redMask = data.palette.redMask;
		greenMask = data.palette.greenMask;
		blueMask = data.palette.blueMask;
	}

	public ImageData createImageData(byte[] data) {
		final PaletteData palette = new PaletteData(redMask, greenMask, blueMask);
		return new ImageData(width, height, depth, palette, scanlinePad, data);
	}
}
