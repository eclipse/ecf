package org.eclipse.ecf.internal.irc.hyperlink;

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

public class IRCHyperlinkDetector extends URLHyperlinkDetector {

	public static final String IRC_PROTOCOL = "irc";

	public IRCHyperlinkDetector() {
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null)
			return null;

		IDocument document = textViewer.getDocument();

		int offset = region.getOffset();

		String urlString = null;
		if (document == null)
			return null;

		IRegion lineInfo;
		String line;
		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			line = document.get(lineInfo.getOffset(), lineInfo.getLength());
		} catch (BadLocationException ex) {
			return null;
		}

		int offsetInLine = offset - lineInfo.getOffset();

		boolean startDoubleQuote = false;
		int urlOffsetInLine = 0;
		int urlLength = 0;

		int urlSeparatorOffset = line.indexOf("://"); //$NON-NLS-1$
		while (urlSeparatorOffset >= 0) {

			// URL protocol (left to "://")
			urlOffsetInLine = urlSeparatorOffset;
			char ch;
			do {
				urlOffsetInLine--;
				ch = ' ';
				if (urlOffsetInLine > -1)
					ch = line.charAt(urlOffsetInLine);
				startDoubleQuote = ch == '"';
			} while (Character.isUnicodeIdentifierStart(ch));
			urlOffsetInLine++;

			// Right to "://"
			StringTokenizer tokenizer = new StringTokenizer(line
					.substring(urlSeparatorOffset + 3), " \t\n\r\f<>", false); //$NON-NLS-1$
			if (!tokenizer.hasMoreTokens())
				return null;

			urlLength = tokenizer.nextToken().length() + 3 + urlSeparatorOffset
					- urlOffsetInLine;
			if (offsetInLine >= urlOffsetInLine
					&& offsetInLine <= urlOffsetInLine + urlLength)
				break;

			urlSeparatorOffset = line.indexOf("://", urlSeparatorOffset + 1); //$NON-NLS-1$
		}

		if (urlSeparatorOffset < 0)
			return null;

		if (startDoubleQuote) {
			int endOffset = -1;
			int nextDoubleQuote = line.indexOf('"', urlOffsetInLine);
			int nextWhitespace = line.indexOf(' ', urlOffsetInLine);
			if (nextDoubleQuote != -1 && nextWhitespace != -1)
				endOffset = Math.min(nextDoubleQuote, nextWhitespace);
			else if (nextDoubleQuote != -1)
				endOffset = nextDoubleQuote;
			else if (nextWhitespace != -1)
				endOffset = nextWhitespace;
			if (endOffset != -1)
				urlLength = endOffset - urlOffsetInLine;
		}

		// Set and validate URL string
		URI uri = null;
		try {
			urlString = line.substring(urlOffsetInLine, urlOffsetInLine
					+ urlLength);
			uri = new URI(urlString);
			String protocol = uri.getScheme();
			if (protocol == null || !protocol.equals(IRC_PROTOCOL))
				return null;
		} catch (URISyntaxException e) {
			return null;
		}

		IRegion urlRegion = new Region(lineInfo.getOffset() + urlOffsetInLine,
				urlLength);
		return new IHyperlink[] { new IRCHyperlink(urlRegion, uri) };
	}

}
