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

package org.eclipse.ecf.ui.screencapture;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class ScreenCaptureConfirmationDialog extends Dialog {

	final Image image;

	private final int width;

	private final int height;

	private final ID targetID;

	private final IImageSender imageSender;

	public ScreenCaptureConfirmationDialog(Shell shell, ID targetID, Image image, int width, int height, IImageSender imageSender) {
		super(shell);
		this.image = image;
		this.width = width;
		this.height = height;
		this.targetID = targetID;
		this.imageSender = imageSender;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			imageSender.sendImage(targetID, image.getImageData());
		}
		super.buttonPressed(buttonId);
	}

	protected Control createDialogArea(Composite parent) {
		parent = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new FillLayout());
		composite.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(image, 0, 0);
			}
		});
		return parent;
	}

	protected Point getInitialSize() {
		final Point point = super.getInitialSize();
		if (point.x < width) {
			if (point.y < height) {
				return new Point(width, height);
			}
			return new Point(width, point.y);
		}
		if (point.y < height) {
			return new Point(point.x, height);
		}
		return new Point(point.x, point.y);
	}

	public boolean close() {
		image.dispose();
		return super.close();
	}

}