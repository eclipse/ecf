package org.eclipse.ecf.internal.irc.ui.hyperlink;

import java.util.StringTokenizer;

import org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerView;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class IRCChannelHyperlinkDetector extends
		org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector {
	public static final String DEFAULT_PREFIX = "#"; //$NON-NLS-1$
	public static final String DEFAULT_ENDDELIMITERS = " \t\n\r\f<>"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null)
			return null;

		ChatRoomManagerView view = (ChatRoomManagerView) getAdapter(ChatRoomManagerView.class);

		IDocument document = textViewer.getDocument();
		if (document == null)
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

		Region detectedRegion = detectRegion(lineInfo, line, offset
				- lineInfo.getOffset());

		if (detectedRegion == null)
			return null;

		int detectedOffset = detectedRegion.getOffset() - lineInfo.getOffset();

		return createHyperLinksForChannel(view, line.substring(detectedOffset,
				detectedOffset + detectedRegion.getLength()), detectedRegion);

	}

	private Region detectRegion(IRegion lineInfo, String fromLine,
			int offsetInLine) {
		int resultLength = 0;

		int separatorOffset = fromLine.indexOf(DEFAULT_PREFIX);
		while (separatorOffset >= 0) {
			StringTokenizer tokenizer = new StringTokenizer(fromLine
					.substring(separatorOffset + DEFAULT_PREFIX.length()),
					DEFAULT_ENDDELIMITERS, false);
			if (!tokenizer.hasMoreTokens())
				return null;

			resultLength = tokenizer.nextToken().length()
					+ DEFAULT_PREFIX.length();
			if (offsetInLine >= separatorOffset
					&& offsetInLine <= separatorOffset + resultLength)
				break;

			separatorOffset = fromLine.indexOf(DEFAULT_PREFIX,
					separatorOffset + 1);
		}

		if (separatorOffset < 0)
			return null;

		return new Region(lineInfo.getOffset() + separatorOffset, resultLength);
	}

	private IHyperlink[] createHyperLinksForChannel(ChatRoomManagerView view,
			String channel, Region region) {
		return new IHyperlink[] { new IRCChannelHyperlink(view, channel, region) };
	}
}
