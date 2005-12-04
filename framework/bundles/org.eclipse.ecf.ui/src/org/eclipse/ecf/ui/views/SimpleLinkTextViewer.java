/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple link text viewer wraps a read-only SWT StyledText and allows plain
 * text and links to be appended to the text. For each link, a runnable is
 * maintained that will be run when the user clicks on the link.
 * 
 * @since 0.5.4
 * 
 */
public class SimpleLinkTextViewer {

	private static Color hyperlinkColor = null;

	private static Color getHyperlinkColor() {
		if (hyperlinkColor == null) {
			hyperlinkColor = new Color(Display.getDefault(), 0, 0, 255);
		}
		return hyperlinkColor;
	}

	private Cursor cursor;

	private List links = new ArrayList();

	private StyledText styledText;

	/**
	 * Creates a new chat text viewer in the given composite.
	 * 
	 * @param composite
	 */
	public SimpleLinkTextViewer(Composite composite) {
		styledText = new StyledText(composite, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.WRAP | SWT.BORDER | SWT.READ_ONLY);
		styledText.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				LinkInfo linkInfo = findLinkInfo(e);
				if (linkInfo != null) {
					linkInfo.runnable.run();
				}
			}
		});
		styledText.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				LinkInfo linkInfo = findLinkInfo(e);
				if (linkInfo != null) {
					setHandCursor();
				} else {
					resetCursor();
				}
			}
		});
	}

	/**
	 * Appends the given text to the underlying StyledText widget.
	 * 
	 * @param text
	 */
	public void append(String text) {
		styledText.append(text);
	}

	/**
	 * Appends the given text as a link to the underlying StyledText widget. The
	 * given runnable will be run when the user clicks on the link.
	 * 
	 * @param text
	 */
	public void appendLink(String text, Runnable onClick) {
		int start = styledText.getCharCount();
		styledText.replaceTextRange(start, 0, text);
		StyleRange styleRange = new StyleRange();
		styleRange.start = start;
		styleRange.length = text.length();
		styleRange.foreground = getHyperlinkColor();
		styleRange.underline = true;
		styledText.setStyleRange(styleRange);
		addLink(start, text.length(), onClick);
	}

	private void addLink(int start, int length, Runnable onClick) {
		links.add(new LinkInfo(start, length, onClick));
	}

	private LinkInfo findLinkInfo(int offset) {
		// TODO replace with binary search
		for (Iterator it = links.iterator(); it.hasNext();) {
			LinkInfo linkInfo = (LinkInfo) it.next();
			if (linkInfo.start <= offset
					&& offset <= linkInfo.start + linkInfo.length) {
				return linkInfo;
			}
		}
		return null;
	}

	private LinkInfo findLinkInfo(MouseEvent e) {
		Point point = new Point(e.x, e.y);
		LinkInfo linkInfo = null;
		if (styledText.getBounds().contains(point)
				&& styledText.getCharCount() > 0) {
			try {
				int offset = styledText.getOffsetAtLocation(point);
				linkInfo = findLinkInfo(offset);
			} catch (IllegalArgumentException ex) {
				// ignore - event was not over character
			}
		}
		return linkInfo;
	}

	private void resetCursor() {
		styledText.setCursor(null);
		if (cursor != null) {
			cursor.dispose();
			cursor = null;
		}
	}

	private void setHandCursor() {
		Display display = styledText.getDisplay();
		if (cursor == null)
			cursor = new Cursor(display, SWT.CURSOR_HAND);
		styledText.setCursor(cursor);
	}

	/**
	 * main method for testing purposes (right-click and select Run As->SWT
	 * Application)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText(SimpleLinkTextViewer.class.getName());
		shell.setLayout(new FillLayout());
		SimpleLinkTextViewer chatTextViewer = new SimpleLinkTextViewer(shell);
		chatTextViewer.append("Hello world\n");
		chatTextViewer.append("Hello ");
		chatTextViewer.appendLink("linked", new Runnable() {

			public void run() {
				System.out.println("clicked!");
			}
		});
		chatTextViewer.append(" world\n");
		chatTextViewer.append("Hello world\n");
		Text secondText = new Text(shell, SWT.BORDER);
		secondText.setText("some other focusable text");
		secondText.forceFocus();
		shell.layout();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static class LinkInfo {
		private final int start;

		private final int length;

		private final Runnable runnable;

		private LinkInfo(int start, int length, Runnable runnable) {
			this.start = start;
			this.length = length;
			this.runnable = runnable;
		}
	}

}
