package org.eclipse.ecf.presence;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;

/**
 * Chat container
 * @author slewis
 *
 */
public interface IChatRoomContainer extends IContainer {
	
	public void connect(String groupName) throws ContainerConnectException;
	
    /**
     * Setup listener for handling IM messages
     * @param listener
     */
	public void addMessageListener(IMessageListener listener);

	/**
	 * Get interface for sending messages
	 * @return IMessageSender.  Null if no message sender available
	 */
    public IMessageSender getMessageSender();

}
