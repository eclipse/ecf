package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.user.IUser;

public interface ILocalUserSettable {
    
    public void setLocalUser(IUser user, ITextInputHandler inputHandler);
    public void setGroup(ID groupManager);
    public void memberDeparted(ID member);
}
