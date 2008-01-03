/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.example.collab.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class ShowImageShell {

	Shell shell;
	ID senderID;
	ImageWrapper imageWrapper;
	List imageData;

	public ShowImageShell(Display display, ID senderID, ImageWrapper imageWrapper, final DisposeListener disposeListener) {
		this.shell = new Shell(display);
		this.senderID = senderID;
		this.imageWrapper = imageWrapper;
		this.shell.setBounds(0, 0, imageWrapper.width, imageWrapper.height);
		this.imageData = new ArrayList();
		this.shell.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				disposeListener.widgetDisposed(e);
				ShowImageShell.this.senderID = null;
				ShowImageShell.this.imageWrapper = null;
				ShowImageShell.this.imageData = null;
			}
		});
	}

	public void setText(String text) {
		shell.setText(text);
	}

	public void open() {
		shell.open();
	}

	public void close() {
		if (shell != null) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						if (!shell.isDisposed())
							shell.close();
						shell = null;
					} catch (final Exception e) {
						// do nothing
					}
				}
			});
		}
	}

	public ID getSenderID() {
		return senderID;
	}

	public void addData(byte[] bytes) {
		this.imageData.add(bytes);
	}

	public void showImage() {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			if (imageData != null) {
				for (final Iterator i = imageData.iterator(); i.hasNext();) {
					bos.write((byte[]) i.next());
				}
			}
			bos.flush();
		} catch (final IOException e) {
			// should not happen
		}
		imageData.clear();
		final byte[] uncompressedData = uncompress(bos.toByteArray());
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				final Image image = new Image(shell.getDisplay(), imageWrapper.createImageData(uncompressedData));
				ShowImageShell.this.shell.addPaintListener(new PaintListener() {
					public void paintControl(PaintEvent e) {
						e.gc.drawImage(image, 0, 0);
					}
				});
				ShowImageShell.this.shell.redraw();
			}
		});
	}

	private static byte[] uncompress(byte[] source) {
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

}
