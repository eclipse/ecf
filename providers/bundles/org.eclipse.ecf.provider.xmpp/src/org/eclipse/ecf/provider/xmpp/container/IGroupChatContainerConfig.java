/*
 * Created on Mar 21, 2005
 *
 */
package org.eclipse.ecf.provider.xmpp.container;

import org.eclipse.ecf.core.ISharedObjectContainerConfig;

public interface IGroupChatContainerConfig extends ISharedObjectContainerConfig {
    
    public String getRoomName();
    public String getOwnerName();
    public String getNickname();
    public String getPassword();
    
}
