package org.eclipse.ecf.ui;

import org.eclipse.ecf.core.identity.ID;

public interface IMessageViewer {
    
    public void showMessage(ID fromID, ID toID, String message);
}
