/*******************************************************************************
 * Copyright (c) 2006 IBM, Inc and Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Chris Aniszczyk <zx@us.ibm.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tutorial.scribbleshare;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.ViewPart;

public class ScribbleView extends ViewPart {

	private Display display;
	private Canvas canvas;
	// Default color is black
	int red = 0;
	int blue = 0;
	int green = 0;
	// Channel to send data on
	IChannel channel = null;
	
	public void setUserColor(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	public void setChannel(IChannel channel) {
		this.channel = channel;
	}
	public void handleDrawLine(byte [] message) {
		ByteArrayInputStream bins = new ByteArrayInputStream(message);
		DataInputStream dins = new DataInputStream(bins);
		try {
			final int red = dins.readInt();
			final int green = dins.readInt();
			final int blue = dins.readInt();
			final int lastX = dins.readInt();
			final int lastY = dins.readInt();
			final int x = dins.readInt();
			final int y = dins.readInt();
			display.asyncExec(new Runnable() {
				public void run() {
					GC gc = new GC(canvas);
					gc.setForeground(new Color(display,new RGB(red,green,blue)));
					gc.drawLine(lastX, lastY, x, y);
					gc.dispose();
				}});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	protected void sendDrawLine(int lastX, int lastY, int x, int y) {
		if (channel != null) {
			try {
				ByteArrayOutputStream bouts = new ByteArrayOutputStream();
				DataOutputStream douts = new DataOutputStream(bouts);
				douts.writeInt(red);douts.writeInt(green);douts.writeInt(blue);
				douts.writeInt(lastX);douts.writeInt(lastY);douts.writeInt(x);douts.writeInt(y);
				channel.sendMessage(bouts.toByteArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void createPartControl(Composite parent) {
		canvas = new Canvas(parent, SWT.NONE);
		display = parent.getDisplay();
		canvas.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		Listener listener = new Listener () {
			int lastX = 0, lastY = 0;
			public void handleEvent (Event event) {
				switch (event.type) {
				case SWT.MouseMove:
					if ((event.stateMask & SWT.BUTTON1) == 0) break;
					GC gc = new GC(canvas);
					gc.drawLine(lastX, lastY, event.x, event.y);
					// Here is where we send the coords to remotes
					sendDrawLine(lastX, lastY, event.x, event.y);
					gc.dispose();
				case SWT.MouseDown:
					lastX = event.x;
					lastY = event.y;
					break;
				}
			}
		};
		canvas.addListener(SWT.MouseDown, listener);
		canvas.addListener(SWT.MouseMove, listener);
	}

	public void setFocus() {
		canvas.setFocus();
	}

}
