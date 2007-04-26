/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.ui.hyperlink;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;

/**
 * 
 */
public abstract class AbstractURLHyperlinkDetector extends
		org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector {

	public static final String DEFAULT_DETECTABLE = "://";
	public static final String DEFAULT_ENDDELIMITERS = " \t\n\r\f<>";

	String[] protocols = null;

	URLHyperlinkDetector urlDetector = new URLHyperlinkDetector();

	protected void setProtocols(String[] protocols) {
		this.protocols = protocols;
	}

	protected String[] getProtocols() {
		return protocols;
	}

	protected String detectSubstring(String fromLine, int offsetInLine) {
		boolean startDoubleQuote = false;
		int detectableOffsetInLine = 0;
		int resultLength = 0;

		int separatorOffset = fromLine.indexOf(DEFAULT_DETECTABLE);
		while (separatorOffset >= 0) {

			detectableOffsetInLine = separatorOffset;
			char ch;
			do {
				detectableOffsetInLine--;
				ch = ' ';
				if (detectableOffsetInLine > -1)
					ch = fromLine.charAt(detectableOffsetInLine);
				startDoubleQuote = ch == '"';
			} while (Character.isUnicodeIdentifierStart(ch));
			detectableOffsetInLine++;

			StringTokenizer tokenizer = new StringTokenizer(fromLine
					.substring(separatorOffset + DEFAULT_DETECTABLE.length()),
					DEFAULT_ENDDELIMITERS, false);
			if (!tokenizer.hasMoreTokens())
				return null;

			resultLength = tokenizer.nextToken().length() + 3 + separatorOffset
					- detectableOffsetInLine;
			if (offsetInLine >= detectableOffsetInLine
					&& offsetInLine <= detectableOffsetInLine + resultLength)
				break;

			separatorOffset = fromLine.indexOf(DEFAULT_DETECTABLE, separatorOffset + 1);
		}

		if (separatorOffset < 0)
			return null;

		if (startDoubleQuote) {
			int endOffset = -1;
			int nextDoubleQuote = fromLine.indexOf('"', detectableOffsetInLine);
			int nextWhitespace = fromLine.indexOf(' ', detectableOffsetInLine);
			if (nextDoubleQuote != -1 && nextWhitespace != -1)
				endOffset = Math.min(nextDoubleQuote, nextWhitespace);
			else if (nextDoubleQuote != -1)
				endOffset = nextDoubleQuote;
			else if (nextWhitespace != -1)
				endOffset = nextWhitespace;
			if (endOffset != -1)
				resultLength = endOffset - detectableOffsetInLine;
		}

		return fromLine.substring(detectableOffsetInLine,
				detectableOffsetInLine + resultLength);
	}

	protected URI detectProtocol(String uriString) {
		if (uriString == null)
			return null;
		URI uri = null;
		try {
			uri = new URI(uriString);
			String protocol = uri.getScheme();
			if (protocol == null || protocols == null)
				return null;
			for (int i = 0; i < protocols.length; i++) {
				if (protocols[i].equalsIgnoreCase(protocol))
					return uri;
			}
		} catch (URISyntaxException e) {
			return null;
		}
		return null;
	}

	/**
	 * Create hyperlinks for detected URI.
	 * 
	 * @param region
	 *            an IRegion to show for hyperlink. Will not be
	 *            <code>null</code>.
	 * @param uri
	 *            a URI to create a hyperlink for. Will not be <code>null</code>.
	 * @return IHyperlink[] of hyperlinks for given URL. Return
	 *         <code>null</code> if no hyperlinks should be displayed for
	 *         detected URI.
	 */
	protected abstract IHyperlink[] createHyperLinksForURI(IRegion region,
			URI uri);

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {

		if (region == null || textViewer == null)
			return null;

		IDocument document = textViewer.getDocument();
		if (document == null)
			return null;

		if (protocols == null)
			return null;

		int offset = region.getOffset();

		IRegion lineInfo;
		String line;
		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			line = document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}

		String detectedSubstring = detectSubstring(line, offset
				- lineInfo.getOffset());
		// Set and validate URL string
		URI uri = detectProtocol(detectedSubstring);
		if (uri == null)
			return null;

		return createHyperLinksForURI(new Region(lineInfo.getOffset()
				+ line.indexOf(detectedSubstring), detectedSubstring.length()),
				uri);
	}

}
