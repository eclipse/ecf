package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.core.identity.ID;

public interface ITextInputHandler {
    public void handleTextLine(ID userID, String text);
    public void handleStartTyping(ID userID);
    public void disconnect();
}
