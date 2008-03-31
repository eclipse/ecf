package org.eclipse.ecf.internal.irc.ui.hyperlink;

import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
import org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerView;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class IRCChannelHyperlink implements IHyperlink {
	private Region region;

	private String channel;

	private String typeLabel;

	private String hyperlinkText;

	private ChatRoomManagerView view;

	public IRCChannelHyperlink(ChatRoomManagerView view, String channel,
			Region region) {
		this.channel = channel;
		this.region = region;
		this.view = view;
	}

	public IRegion getHyperlinkRegion() {
		return this.region;
	}

	public String getHyperlinkText() {
		return this.hyperlinkText;
	}

	public String getTypeLabel() {
		return this.typeLabel;
	}

	public void open() {
		IChatRoomManager manager = (IChatRoomManager) view.getRootChatRoomContainer();

		view.joinRoom(manager.getChatRoomInfo(channel), "");
	}

}
