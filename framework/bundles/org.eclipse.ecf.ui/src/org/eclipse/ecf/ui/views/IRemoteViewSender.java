package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.core.identity.ID;

public interface IRemoteViewSender {
	public void sendShowView(ID target, String viewid);
}
