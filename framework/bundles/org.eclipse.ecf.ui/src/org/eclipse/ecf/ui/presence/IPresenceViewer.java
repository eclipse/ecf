/*
 * Created on Feb 14, 2005
 *
 */
package org.eclipse.ecf.ui.presence;

import org.eclipse.ecf.core.identity.ID;

public interface IPresenceViewer {
    
    public void receivePresence(ID userID, IPresence presence);
}
